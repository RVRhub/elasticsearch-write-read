package dev.rvr.elasticsearchwriteread.listener

import com.fasterxml.jackson.databind.ObjectMapper
import dev.rvr.elasticsearchwriteread.configurations.RabbitMqConfiguration
import dev.rvr.elasticsearchwriteread.model.Product
import dev.rvr.elasticsearchwriteread.service.SynchronizationService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

@Component
class ReactiveRabbitListener(
    connectionFactory: ConnectionFactory
) {

    private lateinit var messageSink: FluxSink<Product>
    private val messageFlux: Flux<Product>

    init {
        val emitter = Flux.create<Product> { sink -> messageSink = sink }.publish()
        messageFlux = emitter.autoConnect()

        val container = SimpleMessageListenerContainer(connectionFactory).apply {
            setQueueNames(RabbitMqConfiguration.DOCUMENTS_QUEUE_NAME)
            setPrefetchCount(50)
            setBatchSize(50)
            setupMessageListener { message: Message ->
                val objectMapper = ObjectMapper()
                val product = objectMapper.readValue(message.body, Product::class.java)
                messageSink.next(product)
            }
            start()
        }
    }

    fun getMessageFlux(): Flux<Product> = messageFlux
}

@Service
class MessageProcessingService(
    private val reactiveRabbitListener: ReactiveRabbitListener,
    val synchronizationService: SynchronizationService
) {

    @PostConstruct
    fun init() {
        startProcessing()
    }

    fun startProcessing() {
        LOG.info("Starting to process messages")
        reactiveRabbitListener.getMessageFlux()
            .buffer(50)
            .flatMap { messages ->
                synchronizationService.addDocuments(messages)
            }
            .subscribe()
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(MessageProcessingService::class.java)
    }
}
