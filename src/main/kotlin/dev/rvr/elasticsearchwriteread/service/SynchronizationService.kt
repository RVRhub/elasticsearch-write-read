package dev.rvr.elasticsearchwriteread.service

import co.elastic.clients.elasticsearch.core.BulkResponse
import dev.rvr.elasticsearchwriteread.model.Product
import dev.rvr.elasticsearchwriteread.repository.ElasticsearchProductRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


@Service
class SynchronizationService(
    private val productRepository: ElasticsearchProductRepository
) {
    fun addDocument(product: Product?): Mono<Void> {
        return productRepository.saveWithoutRefresh(product)
            .doOnSuccess { LOG.info("Document saved: {}", it) }
            .doOnError { LOG.error("Couldn't save Document.", it) }
            .then()

    }

    fun addDocuments(products: List<Product>): Mono<BulkResponse> {
        return productRepository.saveBulk(products)
            .doOnSuccess{ LOG.info("Documents saved: {}", products.size) }
            .doOnError { LOG.error("Couldn't save Documents.") }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(SynchronizationService::class.java)
    }
}