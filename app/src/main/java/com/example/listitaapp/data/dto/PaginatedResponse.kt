package com.example.listitaapp.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaginatedResponse<T>(
    @Json(name = "data")
    val data: List<T>,

    @Json(name = "pagination")
    val pagination: Pagination
)

@JsonClass(generateAdapter = true)
data class Pagination(
    @Json(name = "total")
    val total: Int,

    @Json(name = "page")
    val page: Int,

    @Json(name = "per_page")
    val perPage: Int,

    @Json(name = "total_pages")
    val totalPages: Int,

    @Json(name = "has_next")
    val hasNext: Boolean,

    @Json(name = "has_prev")
    val hasPrev: Boolean
)
