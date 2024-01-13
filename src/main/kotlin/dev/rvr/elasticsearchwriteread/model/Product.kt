package dev.rvr.elasticsearchwriteread.model

import java.io.Serializable

data class Product(
    var id: String? = null,
    var name: String? = "",
    var description: String? = "",
    var price: String? = ""
) : Serializable