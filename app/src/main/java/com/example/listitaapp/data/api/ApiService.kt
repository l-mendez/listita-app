package com.example.listitaapp.data.api

import com.example.listitaapp.data.dto.*
import com.example.listitaapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== Authentication Endpoints ==========

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/users/verify-account")
    suspend fun verifyAccount(@Body request: VerifyAccountRequest): Response<User>

    @POST("api/users/send-verification")
    suspend fun resendVerification(@Query("email") email: String): Response<ResendVerificationResponse>

    @POST("api/users/forgot-password")
    suspend fun forgotPassword(@Body request: PasswordRecoveryRequest): Response<Unit>

    @POST("api/users/reset-password")
    suspend fun resetPassword(@Body request: PasswordResetRequest): Response<Unit>

    @POST("api/users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // ========== User Endpoints ==========

    @GET("api/users/profile")
    suspend fun getProfile(): Response<User>

    @PUT("api/users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<User>

    // ========== Category Endpoints ==========

    @GET("api/categories")
    suspend fun getCategories(): Response<PaginatedResponse<Category>>

    @POST("api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<Category>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): Response<Category>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body request: UpdateCategoryRequest
    ): Response<Category>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long): Response<Unit>

    // ========== Product Endpoints ==========

    @GET("api/products")
    suspend fun getProducts(): Response<PaginatedResponse<Product>>

    @POST("api/products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<Product>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: UpdateProductRequest
    ): Response<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<Unit>

    // ========== Shopping List Endpoints ==========

    @GET("api/shopping-lists")
    suspend fun getShoppingLists(): Response<PaginatedResponse<ShoppingList>>

    @POST("api/shopping-lists")
    suspend fun createShoppingList(@Body request: CreateShoppingListRequest): Response<ShoppingList>

    @GET("api/shopping-lists/{id}")
    suspend fun getShoppingListById(@Path("id") id: Long): Response<ShoppingList>

    @PUT("api/shopping-lists/{id}")
    suspend fun updateShoppingList(
        @Path("id") id: Long,
        @Body request: UpdateShoppingListRequest
    ): Response<ShoppingList>

    @DELETE("api/shopping-lists/{id}")
    suspend fun deleteShoppingList(@Path("id") id: Long): Response<Unit>

    @POST("api/shopping-lists/{id}/share")
    suspend fun shareShoppingList(
        @Path("id") id: Long,
        @Body request: ShareListRequest
    ): Response<Unit>

    @GET("api/shopping-lists/{id}/shared-users")
    suspend fun getSharedUsers(@Path("id") id: Long): Response<List<User>>

    @DELETE("api/shopping-lists/{id}/share/{user_id}")
    suspend fun revokeShare(
        @Path("id") id: Long,
        @Path("user_id") userId: Long
    ): Response<Unit>

    @POST("api/shopping-lists/{id}/purchase")
    suspend fun purchaseShoppingList(@Path("id") id: Long): Response<Unit>

    @POST("api/shopping-lists/{id}/reset")
    suspend fun resetShoppingList(@Path("id") id: Long): Response<Unit>

    // ========== List Item Endpoints ==========

    @GET("api/shopping-lists/{id}/items")
    suspend fun getListItems(@Path("id") listId: Long): Response<PaginatedResponse<ListItem>>

    @POST("api/shopping-lists/{id}/items")
    suspend fun addListItem(
        @Path("id") listId: Long,
        @Body request: AddListItemRequest
    ): Response<AddListItemResponse>

    @PUT("api/shopping-lists/{id}/items/{item_id}")
    suspend fun updateListItem(
        @Path("id") listId: Long,
        @Path("item_id") itemId: Long,
        @Body request: UpdateListItemRequest
    ): Response<ListItem>

    @PATCH("api/shopping-lists/{id}/items/{item_id}")
    suspend fun toggleItemPurchased(
        @Path("id") listId: Long,
        @Path("item_id") itemId: Long,
        @Body request: ToggleItemPurchasedRequest
    ): Response<ListItem>

    @DELETE("api/shopping-lists/{id}/items/{item_id}")
    suspend fun deleteListItem(
        @Path("id") listId: Long,
        @Path("item_id") itemId: Long
    ): Response<Unit>
}
