package dev.rvr.elasticsearchwriteread.repository

import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.BulkResponse
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.IndexResponse
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation
import dev.rvr.elasticsearchwriteread.model.Product
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ElasticsearchProductRepositoryInternal

@Component
class ElasticsearchProductRepository(
    private val reactiveElasticsearchClient: ReactiveElasticsearchClient,
    private val elasticsearchConverter: ElasticsearchConverter
) : ElasticsearchProductRepositoryInternal {

    fun saveWithoutRefresh(product: Product?): Mono<Product> {
        val indexRequest = IndexRequest.Builder<Document>().index("products")

        val id = product?.id
        if (id != null) {
            indexRequest.id(id)
        }

        val request = indexRequest.document(elasticsearchConverter.mapObject(product)).build()
        return reactiveElasticsearchClient.index(request).map { indexResponse: IndexResponse ->
            val assignedId = indexResponse.id()
            product?.id = assignedId
            product
        }
    }

    fun saveBulk(products: List<Product>): Mono<BulkResponse> {
        val bulkOperations = products.map { product ->
            val document = elasticsearchConverter.mapObject(product)
            val indexOperation = IndexOperation.Builder<Document>().index("products").document(document).build()
            BulkOperation.Builder().index(indexOperation).build()
        }

        val bulkRequest = BulkRequest.Builder().operations(bulkOperations).build()
        return reactiveElasticsearchClient.bulk(bulkRequest)
    }
}