package com.example.listitaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.listitaapp.ui.auth.AuthViewModel
import com.example.listitaapp.ui.auth.LoginScreen
import com.example.listitaapp.ui.auth.RegisterScreen
import com.example.listitaapp.ui.auth.VerifyAccountScreen
import com.example.listitaapp.ui.categories.CategoryViewModel
import com.example.listitaapp.ui.components.AddProductDialog
import com.example.listitaapp.ui.components.CreateCategoryDialog
import com.example.listitaapp.ui.lists.*
import com.example.listitaapp.ui.navigation.Screen
import com.example.listitaapp.ui.products.*
import com.example.listitaapp.ui.profile.*
import com.example.listitaapp.ui.theme.ListitaAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val shoppingListViewModel: ShoppingListViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val purchaseHistoryViewModel: com.example.listitaapp.ui.purchases.PurchaseHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ListitaAppTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    productViewModel = productViewModel,
                    categoryViewModel = categoryViewModel,
                    shoppingListViewModel = shoppingListViewModel,
                    profileViewModel = profileViewModel,
                    purchaseHistoryViewModel = purchaseHistoryViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    profileViewModel: ProfileViewModel,
    purchaseHistoryViewModel: com.example.listitaapp.ui.purchases.PurchaseHistoryViewModel
) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()

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
        if (!authUiState.isInitialAuthCheckComplete) {
            // Show loading screen while checking authentication
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = if (authUiState.isAuthenticated) {
                    Screen.ShoppingLists.route
                } else {
                    Screen.Login.route
                }
            ) {
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
                        viewModel = productViewModel,
                        categoryViewModel = categoryViewModel
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

            composable(
                route = Screen.ListDetail.route,
                arguments = listOf(navArgument("listId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
                MainScreenScaffold(
                    navController = navController,
                    currentRoute = Screen.ListDetail.route
                ) {
                    ShoppingListDetailScreenWrapper(
                        listId = listId,
                        viewModel = shoppingListViewModel,
                        productViewModel = productViewModel,
                        categoryViewModel = categoryViewModel,
                        navController = navController
                    )
                }
            }

            composable(Screen.PurchaseHistory.route) {
                MainScreenScaffold(
                    navController = navController,
                    currentRoute = Screen.PurchaseHistory.route
                ) {
                    PurchaseHistoryScreenWrapper(
                        purchaseHistoryViewModel = purchaseHistoryViewModel,
                        shoppingListViewModel = shoppingListViewModel,
                        navController = navController
                    )
                }
            }
            }
        }
    }
}

@Composable
fun MainScreenScaffold(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.width(232.dp))
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .padding(start = 16.dp, top = 20.dp, bottom = 20.dp)
                    .width(200.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                CustomNavigationRailItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Lists",
                    selected = currentRoute == Screen.ShoppingLists.route,
                    onClick = {
                        if (currentRoute != Screen.ShoppingLists.route) {
                            navController.navigate(Screen.ShoppingLists.route) {
                                popUpTo(Screen.ShoppingLists.route) { inclusive = true }
                            }
                        }
                    }
                )
                CustomNavigationRailItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "Products",
                    selected = currentRoute == Screen.Products.route,
                    onClick = {
                        if (currentRoute != Screen.Products.route) {
                            navController.navigate(Screen.Products.route) {
                                popUpTo(Screen.ShoppingLists.route) { inclusive = false }
                            }
                        }
                    }
                )
                CustomNavigationRailItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
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
        }
    } else {
        Scaffold(
            bottomBar = {
                Column {
                    Divider()
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(Modifier.width(8.dp))
                        BottomNavItem(
                            icon = Icons.AutoMirrored.Filled.List,
                            label = "Lists",
                            selected = currentRoute == Screen.ShoppingLists.route
                        ) {
                            if (currentRoute != Screen.ShoppingLists.route) {
                                navController.navigate(Screen.ShoppingLists.route) {
                                    popUpTo(Screen.ShoppingLists.route) { inclusive = true }
                                }
                            }
                        }
                        BottomNavItem(
                            icon = Icons.Default.ShoppingCart,
                            label = "Products",
                            selected = currentRoute == Screen.Products.route
                        ) {
                            if (currentRoute != Screen.Products.route) {
                                navController.navigate(Screen.Products.route) {
                                    popUpTo(Screen.ShoppingLists.route) { inclusive = false }
                                }
                            }
                        }
                        BottomNavItem(
                            icon = Icons.Default.Person,
                            label = "Profile",
                            selected = currentRoute == Screen.Profile.route
                        ) {
                            if (currentRoute != Screen.Profile.route) {
                                navController.navigate(Screen.Profile.route) {
                                    popUpTo(Screen.ShoppingLists.route) { inclusive = false }
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        }
    }
}

@Composable
private fun CustomNavigationRailItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        Color(0xFFE5E5E5)
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        Color(0xFFE5E5E5) // Light gray
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = contentColor)
            Text(text = label, color = contentColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun ShoppingListsScreenWrapper(
    viewModel: ShoppingListViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Reload lists whenever this screen is displayed (e.g., after restoring from history)
    LaunchedEffect(Unit) {
        viewModel.loadShoppingLists()
    }

    ShoppingListsScreen(
        uiState = uiState,
        onCreateList = { showCreateDialog = true },
        onListClick = { listId ->
            navController.navigate(Screen.ListDetail.createRoute(listId))
        },
        onDeleteList = { viewModel.deleteShoppingList(it) },
        onUpdateListName = { id, name -> viewModel.updateListName(id, name) },
        onUpdateListDescription = { id, description -> viewModel.updateListDescription(id, description) },
        onToggleRecurring = { id, current -> viewModel.toggleListRecurring(id, current) },
        onShareByEmail = { id, email -> viewModel.shareListByEmail(id, email) },
        onLoadSharedUsers = { id -> viewModel.loadSharedUsers(id) },
        onRevokeShare = { id, userId -> viewModel.revokeUserAccess(id, userId) },
        onMakePrivate = { id -> viewModel.makeListPrivate(id) },
        onNavigateToHistory = {
            navController.navigate(Screen.PurchaseHistory.route)
        },
        onRefresh = { viewModel.loadShoppingLists() },
        onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
        onSearch = { viewModel.searchShoppingLists() },
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
fun ProductsScreenWrapper(
    viewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    ProductsScreen(
        uiState = uiState,
        categories = categoryUiState.categories,
        onCreateProduct = { showAddProductDialog = true },
        onDeleteProduct = { viewModel.deleteProduct(it) },
        onUpdateProduct = { id, name, price, categoryId ->
            val metadata = if (price.isNullOrBlank()) null else mapOf<String, Any>("price" to price)
            viewModel.updateProduct(id, name, categoryId, metadata)
        },
        onCreateCategory = { showCreateCategoryDialog = true },
        onRefresh = {
            viewModel.loadProducts()
            categoryViewModel.loadCategories()
        },
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onSearch = { viewModel.searchProducts() },
        onClearError = { viewModel.clearError() },
        onClearSuccess = { viewModel.clearSuccess() }
    )

    if (showAddProductDialog) {
        AddProductDialog(
            categories = categoryUiState.categories,
            onDismiss = { showAddProductDialog = false },
            onCreateProduct = { name, categoryId ->
                viewModel.createProduct(name, categoryId)
                showAddProductDialog = false
            },
            onCreateCategory = { name, onCreated ->
                categoryViewModel.createCategory(name) { category ->
                    onCreated(category)
                }
            }
        )
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onCreate = { name ->
                categoryViewModel.createCategory(name)
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
    productViewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val productUiState by productViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var resumeAddItemAfterProduct by remember { mutableStateOf(false) }

    LaunchedEffect(listId) {
        viewModel.loadListDetails(listId)
    }

    // Handle navigation back when list is completed
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            viewModel.clearNavigateBack()
            navController.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentList()
        }
    }

    ShoppingListDetailScreen(
        listId = listId,
        uiState = uiState,
        onBack = { navController.popBackStack() },
        onAddItem = {
            showAddItemDialog = true
        },
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
            products = productUiState.products,
            recentProduct = productUiState.recentlyCreatedProduct,
            onCreateNewProduct = {
                productViewModel.loadProducts()
                categoryViewModel.loadCategories()
                resumeAddItemAfterProduct = true
                showAddItemDialog = false
                showAddProductDialog = true
            },
            onClearRecentProduct = { productViewModel.clearRecentlyCreatedProduct() },
            onDismiss = { showAddItemDialog = false },
            onAdd = { productId, quantity, unit ->
                viewModel.addItemToList(listId, productId, quantity, unit)
                showAddItemDialog = false
            }
        )
    }

    if (showAddProductDialog) {
        AddProductDialog(
            categories = categoryUiState.categories,
            onDismiss = {
                showAddProductDialog = false
                if (resumeAddItemAfterProduct) {
                    resumeAddItemAfterProduct = false
                    showAddItemDialog = true
                }
            },
            onCreateProduct = { name, categoryId ->
                productViewModel.createProduct(name, categoryId)
                showAddProductDialog = false
                if (resumeAddItemAfterProduct) {
                    resumeAddItemAfterProduct = false
                    showAddItemDialog = true
                }
            },
            onCreateCategory = { name, onCreated ->
                categoryViewModel.createCategory(name) { category ->
                    onCreated(category)
                }
            }
        )
    }
}

@Composable
fun PurchaseHistoryScreenWrapper(
    purchaseHistoryViewModel: com.example.listitaapp.ui.purchases.PurchaseHistoryViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    navController: NavHostController
) {
    val uiState by purchaseHistoryViewModel.uiState.collectAsState()

    // Reload purchase history whenever this screen is displayed
    LaunchedEffect(Unit) {
        purchaseHistoryViewModel.loadPurchaseHistory()
    }

    com.example.listitaapp.ui.purchases.PurchaseHistoryScreen(
        uiState = uiState,
        onRestorePurchase = { purchaseId ->
            purchaseHistoryViewModel.restorePurchase(purchaseId, shoppingListViewModel)
        },
        onNavigateBack = {
            navController.navigate(Screen.ShoppingLists.route) {
                popUpTo(Screen.ShoppingLists.route) { inclusive = false }
            }
        },
        onClearError = { purchaseHistoryViewModel.clearError() },
        onClearSuccess = { purchaseHistoryViewModel.clearSuccess() }
    )
}
