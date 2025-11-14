package com.example.listitaapp.ui.navigation

sealed class Screen(val route: String) {
    // Authentication
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object VerifyAccount : Screen("verify_account/{email}") {
        fun createRoute(email: String) = "verify_account/$email"
    }

    // Main screens
    data object ShoppingLists : Screen("shopping_lists")
    data object Products : Screen("products")
    data object Profile : Screen("profile")

    // Detail screens
    data object ListDetail : Screen("list_detail/{listId}") {
        fun createRoute(listId: Long) = "list_detail/$listId"
    }
    data object PurchaseHistory : Screen("purchase_history")
    data object Settings : Screen("settings")
    data object ChangePassword : Screen("change_password")
}
