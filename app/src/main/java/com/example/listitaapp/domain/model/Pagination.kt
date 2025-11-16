package com.example.listitaapp.domain.model

data class PaginationInfo(
    val total: Int,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

data class PaginatedResult<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)
