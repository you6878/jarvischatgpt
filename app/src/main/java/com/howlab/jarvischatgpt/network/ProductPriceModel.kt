package com.howlab.jarvischatgpt.network

data class ProductPriceModel(
    var name : String = "",
    var min_price : Int = 0,
    var max_price : Int = 0
)