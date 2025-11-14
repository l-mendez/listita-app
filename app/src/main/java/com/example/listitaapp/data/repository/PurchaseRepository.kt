package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.model.Purchase
import com.example.listitaapp.data.model.ShoppingList
import javax.inject.Inject

class PurchaseRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getPurchases(
        page: Int = 1,
        perPage: Int = 20
    ): Result<List<Purchase>> = try {
        val response = apiService.getPurchases(page, perPage)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.data)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPurchaseById(id: Long): Result<Purchase> = try {
        val response = apiService.getPurchaseById(id)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun restorePurchase(id: Long): Result<ShoppingList> = try {
        val response = apiService.restorePurchase(id)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.list)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
