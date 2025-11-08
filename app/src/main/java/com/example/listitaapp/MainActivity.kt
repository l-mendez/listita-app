package com.example.listitaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.listitaapp.data.api.ApiClient
import com.example.listitaapp.ui.auth.AuthViewModel
import com.example.listitaapp.ui.auth.LoginScreen
import com.example.listitaapp.ui.auth.RegisterScreen
import com.example.listitaapp.ui.auth.VerifyAccountScreen
import com.example.listitaapp.ui.lists.*
import com.example.listitaapp.ui.navigation.Screen
import com.example.listitaapp.ui.products.*
import com.example.listitaapp.ui.profile.*
import com.example.listitaapp.ui.theme.ListitaAppTheme

/**
 * MainActivity - Entry point of the app
 * Implements adaptive navigation for responsive design across different screen sizes
 * Follows HCI principles for consistent user experience
 */
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val shoppingListViewModel: ShoppingListViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize API Client
        ApiClient.init(this)

        enableEdgeToEdge()
        setContent {
            ListitaAppTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    productViewModel = productViewModel,
                    shoppingListViewModel = shoppingListViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    profileViewModel: ProfileViewModel
) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()

    // Navigate to main screen when user becomes authenticated
    LaunchedEffect(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != Screen.ShoppingLists.route &&
                currentRoute != Screen.Products.route &&
                currentRoute != Screen.Profile.route &&
                currentRoute != Screen.ListDetail.route
            ) {
                navController.navigate(Screen.ShoppingLists.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = if (authUiState.isAuthenticated) {
                Screen.ShoppingLists.route
            } else {
                Screen.Login.route
            }
        ) {
            // Authentication screens
            composable(Screen.Login.route) {
                LaunchedEffect(Unit) {
                    authViewModel.resetRegistrationState()
                }

                LoginScreen(
                    uiState = authUiState,
                    onLogin = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onClearError = {
                        authViewModel.clearError()
                    }
                )
            }

            composable(Screen.Register.route) {
                LaunchedEffect(Unit) {
                    authViewModel.resetRegistrationState()
                }

                RegisterScreen(
                    uiState = authUiState,
                    onRegister = { email, password, name, surname ->
                        authViewModel.register(email, password, name, surname)
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onNavigateToVerify = { email ->
                        navController.navigate(Screen.VerifyAccount.createRoute(email))
                    },
                    onClearError = {
                        authViewModel.clearError()
                    }
                )
            }

            composable(
                route = Screen.VerifyAccount.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerifyAccountScreen(
                    email = email,
                    uiState = authUiState,
                    onVerify = { code ->
                        authViewModel.verifyAccount(code)
                    },
                    onResendCode = {
                        authViewModel.resendVerificationCode()
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onClearError = {
                        authViewModel.clearError()
                    },
                    onClearSuccess = {
                        authViewModel.clearSuccess()
                    }
                )
            }

            // Main app screens with adaptive navigation
            composable(Screen.ShoppingLists.route) {
                MainScreenScaffold(
                    navController = navController,
                    currentRoute = Screen.ShoppingLists.route
                ) {
                    ShoppingListsScreenWrapper(
                        viewModel = shoppingListViewModel,
                        navController = navController
                    )
                }
            }

            composable(Screen.Products.route) {
                MainScreenScaffold(
                    navController = navController,
                    currentRoute = Screen.Products.route
                ) {
                    ProductsScreenWrapper(
                        viewModel = productViewModel
                    )
                }
            }

            composable(Screen.Profile.route) {
                MainScreenScaffold(
                    navController = navController,
                    currentRoute = Screen.Profile.route
                ) {
                    ProfileScreenWrapper(
                        viewModel = profileViewModel,
                        authViewModel = authViewModel,
                        navController = navController
                    )
                }
            }

            // Detail screens
            composable(
                route = Screen.ListDetail.route,
                arguments = listOf(navArgument("listId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
                ShoppingListDetailScreenWrapper(
                    listId = listId,
                    viewModel = shoppingListViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Adaptive navigation scaffold that adjusts navigation UI based on screen size
 * - On phones: Bottom navigation bar
 * - On tablets: Navigation rail or drawer
 */
@Composable
fun MainScreenScaffold(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                icon = { Icon(Icons.Default.List, contentDescription = "Lists") },
                label = { Text("Lists") },
                selected = currentRoute == Screen.ShoppingLists.route,
                onClick = {
                    if (currentRoute != Screen.ShoppingLists.route) {
                        navController.navigate(Screen.ShoppingLists.route) {
                            popUpTo(Screen.ShoppingLists.route) { inclusive = true }
                        }
                    }
                }
            )
            item(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Products") },
                label = { Text("Products") },
                selected = currentRoute == Screen.Products.route,
                onClick = {
                    if (currentRoute != Screen.Products.route) {
                        navController.navigate(Screen.Products.route) {
                            popUpTo(Screen.ShoppingLists.route) { inclusive = false }
                        }
                    }
                }
            )
            item(
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile") },
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    if (currentRoute != Screen.Profile.route) {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.ShoppingLists.route) { inclusive = false }
                        }
                    }
                }
            )
        }
    ) {
        content()
    }
}

@Composable
fun ShoppingListsScreenWrapper(
    viewModel: ShoppingListViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    ShoppingListsScreen(
        uiState = uiState,
        onCreateList = { showCreateDialog = true },
        onListClick = { listId ->
            navController.navigate(Screen.ListDetail.createRoute(listId))
        },
        onDeleteList = { viewModel.deleteShoppingList(it) },
        onRefresh = { viewModel.loadShoppingLists() },
        onClearError = { viewModel.clearError() },
        onClearSuccess = { viewModel.clearSuccess() }
    )

    if (showCreateDialog) {
        CreateShoppingListDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description, recurring ->
                viewModel.createShoppingList(name, description, recurring)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ProductsScreenWrapper(viewModel: ProductViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateProductDialog by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    ProductsScreen(
        uiState = uiState,
        onCreateProduct = { showCreateProductDialog = true },
        onDeleteProduct = { viewModel.deleteProduct(it) },
        onCreateCategory = { showCreateCategoryDialog = true },
        onRefresh = { viewModel.loadProducts() },
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onClearError = { viewModel.clearError() },
        onClearSuccess = { viewModel.clearSuccess() }
    )

    if (showCreateProductDialog) {
        CreateProductDialog(
            categories = uiState.categories,
            onDismiss = { showCreateProductDialog = false },
            onCreate = { name, categoryId ->
                viewModel.createProduct(name, categoryId)
                showCreateProductDialog = false
            }
        )
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onCreate = { name ->
                viewModel.createCategory(name)
                showCreateCategoryDialog = false
            }
        )
    }
}

@Composable
fun ProfileScreenWrapper(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    ProfileScreen(
        uiState = uiState,
        onEditProfile = { showEditDialog = true },
        onChangePassword = { showChangePasswordDialog = true },
        onLogout = {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        },
        onClearError = { viewModel.clearError() },
        onClearSuccess = { viewModel.clearSuccess() }
    )

    if (showEditDialog && uiState.user != null) {
        EditProfileDialog(
            currentName = uiState.user!!.name,
            currentSurname = uiState.user!!.surname,
            onDismiss = { showEditDialog = false },
            onSave = { name, surname ->
                viewModel.updateProfile(name, surname)
                showEditDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChange = { currentPassword, newPassword ->
                viewModel.changePassword(currentPassword, newPassword)
                showChangePasswordDialog = false
            }
        )
    }
}

@Composable
fun ShoppingListDetailScreenWrapper(
    listId: Long,
    viewModel: ShoppingListViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Load list details when screen appears
    LaunchedEffect(listId) {
        viewModel.loadListDetails(listId)
    }

    // Clear current list when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentList()
        }
    }

    ShoppingListDetailScreen(
        listId = listId,
        uiState = uiState,
        onBack = onBack,
        onAddItem = { showAddItemDialog = true },
        onToggleItem = { itemId ->
            viewModel.toggleItemPurchased(listId, itemId)
        },
        onDeleteItem = { itemId ->
            viewModel.deleteListItem(listId, itemId)
        },
        onClearError = { viewModel.clearError() },
        onClearSuccess = { viewModel.clearSuccess() }
    )

    if (showAddItemDialog) {
        AddItemToListDialog(
            products = uiState.availableProducts,
            onDismiss = { showAddItemDialog = false },
            onAdd = { productId, quantity, unit ->
                viewModel.addItemToList(listId, productId, quantity, unit)
                showAddItemDialog = false
            }
        )
    }
}
