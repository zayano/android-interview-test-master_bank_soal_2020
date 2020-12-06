package com.tokopedia.filter.view.models

data class DataProduct (
        val id : Int,
        val name : String,
        val imageUrl : String,
        val priceInt : Int,
        val discountPercentage: Int,
        val slashedPriceInt : Int,
        val shop : ShopDetail
)
