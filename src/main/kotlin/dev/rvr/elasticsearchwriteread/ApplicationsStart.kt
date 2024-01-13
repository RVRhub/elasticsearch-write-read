package dev.rvr.elasticsearchwriteread

import dev.rvr.elasticsearchwriteread.configurations.RabbitMqConfiguration
import dev.rvr.elasticsearchwriteread.model.Product
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

//@Component
class ApplicationStartup(
    private val rabbitTemplate: RabbitTemplate
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMqConfiguration.DIRECT_EXCHANGE_NAME,
            RabbitMqConfiguration.PRODUCTS_ROUTING_KEY,
            Product("1", "Product 1", "Description 1", "1.0")
        )
    }
}