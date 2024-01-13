package dev.rvr.elasticsearchwriteread.configurations

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfiguration {

    @Bean
    fun jsonMessageConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun productsQueue(): Queue {
        return QueueBuilder.durable(DOCUMENTS_QUEUE_NAME).build()
    }

    @Bean
    fun productsBinding(productsQueue: Queue?, distributionGateDirectExchange: DirectExchange?): Binding {
        return BindingBuilder.bind(productsQueue).to(distributionGateDirectExchange)
            .with(PRODUCTS_ROUTING_KEY)
    }

    @Bean
    fun distributionGateDirectExchange(): DirectExchange {
        return ExchangeBuilder.directExchange(DIRECT_EXCHANGE_NAME).build()
    }

    companion object {
        const val DOCUMENTS_QUEUE_NAME: String = "DOCUMENTS_QUEUE"
        const val PRODUCTS_ROUTING_KEY: String = "products.key"
        const val DIRECT_EXCHANGE_NAME: String = "test.direct.exchange"

    }

}