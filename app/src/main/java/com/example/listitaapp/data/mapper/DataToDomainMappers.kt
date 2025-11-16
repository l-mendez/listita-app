package com.example.listitaapp.data.mapper

import com.example.listitaapp.data.dto.Pagination
import com.example.listitaapp.data.dto.PaginatedResponse
import com.example.listitaapp.data.model.Category as NetworkCategory
import com.example.listitaapp.data.model.ListItem as NetworkListItem
import com.example.listitaapp.data.model.Product as NetworkProduct
import com.example.listitaapp.data.model.ShoppingList as NetworkShoppingList
import com.example.listitaapp.data.model.User as NetworkUser
import com.example.listitaapp.data.model.Purchase as NetworkPurchase
import com.example.listitaapp.domain.model.Category as DomainCategory
import com.example.listitaapp.domain.model.ListItem as DomainListItem
import com.example.listitaapp.domain.model.Product as DomainProduct
import com.example.listitaapp.domain.model.ShoppingList as DomainShoppingList
import com.example.listitaapp.domain.model.User as DomainUser
import com.example.listitaapp.domain.model.Purchase as DomainPurchase
import com.example.listitaapp.domain.model.PaginatedResult as DomainPaginatedResult
import com.example.listitaapp.domain.model.PaginationInfo as DomainPaginationInfo

fun NetworkUser.toDomain(): DomainUser = DomainUser(
    id = id,
    email = email,
    name = name,
    surname = surname,
    metadata = metadata,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NetworkCategory.toDomain(): DomainCategory = DomainCategory(
    id = id,
    name = name,
    metadata = metadata,
    owner = owner?.toDomain(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NetworkProduct.toDomain(): DomainProduct = DomainProduct(
    id = id,
    name = name,
    category = category?.toDomain(),
    metadata = metadata,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NetworkListItem.toDomain(): DomainListItem = DomainListItem(
    id = id,
    quantity = quantity,
    unit = unit,
    purchased = purchased,
    lastPurchasedAt = lastPurchasedAt,
    product = product?.toDomain(),
    metadata = metadata,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NetworkShoppingList.toDomain(): DomainShoppingList = DomainShoppingList(
    id = id,
    name = name,
    description = description,
    recurring = recurring,
    owner = owner.toDomain(),
    sharedWith = sharedWith.map { it.toDomain() },
    lastPurchasedAt = lastPurchasedAt,
    metadata = metadata,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NetworkPurchase.toDomain(): DomainPurchase = DomainPurchase(
    id = id,
    list = list?.toDomain(),
    owner = owner?.toDomain(),
    items = items.map { it.toDomain() },
    metadata = metadata,
    createdAt = createdAt
)

fun <T, R> PaginatedResponse<T>.toDomain(
    mapItem: (T) -> R
): DomainPaginatedResult<R> = DomainPaginatedResult(
    data = data.map(mapItem),
    pagination = pagination.toDomain()
)

fun Pagination.toDomain(): DomainPaginationInfo = DomainPaginationInfo(
    total = total,
    page = page,
    perPage = perPage,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrev = hasPrev
)
