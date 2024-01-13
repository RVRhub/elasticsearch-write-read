package dev.rvr.elasticsearchwriteread

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ElasticsearchWriteReadApplication

fun main(args: Array<String>) {
    runApplication<ElasticsearchWriteReadApplication>(*args)
}
