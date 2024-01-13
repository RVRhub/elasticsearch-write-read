package dev.rvr.elasticsearchwriteread.listener

import dev.rvr.elasticsearchwriteread.configurations.RabbitMqConfiguration
import dev.rvr.elasticsearchwriteread.model.Product
import dev.rvr.elasticsearchwriteread.service.SynchronizationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
//import reactor.rabbitmq.Delivery
//import reactor.rabbitmq.ReceiverOptions

@Component
class RabbitMqListener @Autowired constructor(
    val synchronizationService: SynchronizationService,
) {

//    @RabbitListener(
//        id = LISTENER_ID,
//        queues = [RabbitMqConfiguration.DOCUMENTS_QUEUE_NAME],
//        concurrency = "1",
////        autoStartup = "false"
//    )
//    fun onProductMessage(product: Product?): Mono<Void> {
//        LOG.info("Consuming Document message: {}", product)
//        return synchronizationService.addDocument(product)
//            .doOnError { err -> LOG.error("Couldn't consume Document message.", err) }
//
//    }

    @RabbitListener(
        id = LISTENER_ID,
        queues = [RabbitMqConfiguration.DOCUMENTS_QUEUE_NAME],
        concurrency = "1",
//        autoStartup = "false"
    )
    fun onProductMessage(products: List<Product>): Mono<Void> {
        LOG.info("Consuming Document message: {}", products)
        return synchronizationService.addDocuments(products)
            .doOnError { err -> LOG.error("Couldn't consume Document message.", err) }
    }

    fun consumeMessages() {
        val receiverOptions = ReceiverOptions()
        val receiver = Receiver.create(receiverOptions)
        val queueName = "yourQueueName"

        receiver.consumeAutoAck(queueName)
            .map { delivery: Delivery -> convertDeliveryToProduct(delivery) }
            .buffer(50)
            .flatMap { products: List<Product> -> elasticsearchProductRepository.saveBulk(products) }
            .subscribe()
    }

    private fun convertDeliveryToProduct(delivery: Delivery): Product {
        return jsonMessageConverter.fromMessage(delivery.body) as Product
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(RabbitMqListener::class.java)

        const val LISTENER_ID: String = "ProductsListener"
    }
}