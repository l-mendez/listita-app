package com.example.listitaapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute {
    const val route = "login"
}

@Serializable
object RegisterRoute {
    const val route = "register"
}

@Serializable
data class VerifyAccountRoute(val email: String) {
    companion object {
        const val route = "verify_account"
    }
}

@Serializable
object ShoppingListsRoute {
    const val route = "shopping_lists"
}

@Serializable
object ProductsRoute {
    const val route = "products"
}

@Serializable
object ProfileRoute {
    const val route = "profile"
}

@Serializable
data class ListDetailRoute(val listId: Long) {
    companion object {
        const val route = "list_detail"
    }
}

@Serializable
object PurchaseHistoryRoute {
    const val route = "purchase_history"
}
