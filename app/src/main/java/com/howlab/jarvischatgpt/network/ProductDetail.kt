package com.howlab.jarvischatgpt.network

data class ProductDetail(
    val description: String,
    val images: List<String>,
    val title: String,
    val price: Int,
)