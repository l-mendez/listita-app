package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.PaginatedResponse
import com.example.listitaapp.data.dto.RestorePurchaseResponse
import com.example.listitaapp.data.model.Purchase
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PurchaseRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    json: Json
) : BaseRemoteDataSource(json) {

    suspend fun getPurchases(
        page: Int = 1,
        perPage: Int = 20,
        sortBy: String = "createdAt",
        order: String = "DESC"
    ): PaginatedResponse<Purchase> {
        val response = apiService.getPurchases(page, perPage, sortBy, order)
        return handleResponse(response, "Failed to get purchases")
    }

    suspend fun getPurchaseById(id: Long): Purchase {
        val response = apiService.getPurchaseById(id)
        return handleResponse(response, "Failed to get purchase")
    }

    suspend fun restorePurchase(id: Long): RestorePurchaseResponse {
        val response = apiService.restorePurchase(id)
        return handleResponse(response, "Failed to restore purchase")
    }
}
