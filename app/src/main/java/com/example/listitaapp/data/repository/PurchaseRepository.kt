package com.example.listitaapp.data.repository

import com.example.listitaapp.data.datasource.PurchaseRemoteDataSource
import com.example.listitaapp.data.mapper.toDomain
import com.example.listitaapp.domain.model.Purchase
import com.example.listitaapp.domain.model.ShoppingList
import javax.inject.Inject

class PurchaseRepository @Inject constructor(
    private val remoteDataSource: PurchaseRemoteDataSource
) {

    suspend fun getPurchases(
        page: Int = 1,
        perPage: Int = 20
    ): Result<List<Purchase>> = runCatching {
        remoteDataSource.getPurchases(page, perPage, sortBy = "createdAt", order = "DESC").data.map { it.toDomain() }
    }

    suspend fun restorePurchase(id: Long): Result<ShoppingList> = runCatching {
        remoteDataSource.restorePurchase(id).list.toDomain()
    }
}
