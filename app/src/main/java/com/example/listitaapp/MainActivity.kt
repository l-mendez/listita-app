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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.listitaapp.data.api.ApiClient
import com.example.listitaapp.ui.auth.AuthViewModel
import com.example.listitaapp.ui.auth.LoginScreen
import com.example.listitaapp.ui.auth.RegisterScreen
import com.example.listitaapp.ui.auth.VerifyAccountScreen
import com.example.listitaapp.ui.navigation.Screen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.listitaapp.ui.theme.ListitaAppTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize API Client
        ApiClient.init(this)

        enableEdgeToEdge()
        setContent {
            ListitaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(authViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsState()

    // Navigate to main screen when user becomes authenticated (e.g., after login or verification)
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            // Check if we're not already on a main screen to avoid redundant navigation
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != Screen.ShoppingLists.route &&
                currentRoute != Screen.Products.route &&
                currentRoute != Screen.Profile.route) {
                navController.navigate(Screen.ShoppingLists.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (uiState.isAuthenticated) {
            Screen.ShoppingLists.route
        } else {
            Screen.Login.route
        }
    ) {
        // Authentication screens
        composable(Screen.Login.route) {
            // Reset registration state when on login screen
            LaunchedEffect(Unit) {
                authViewModel.resetRegistrationState()
            }

            LoginScreen(
                uiState = uiState,
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
            // Reset registration state when entering register screen
            LaunchedEffect(Unit) {
                authViewModel.resetRegistrationState()
            }

            RegisterScreen(
                uiState = uiState,
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
                uiState = uiState,
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

        // Main screens (placeholders for now)
        composable(Screen.ShoppingLists.route) {
            MainScreenWithNavigation(
                currentRoute = Screen.ShoppingLists.route,
                navController = navController
            ) {
                PlaceholderScreen("Shopping Lists")
            }
        }

        composable(Screen.Products.route) {
            MainScreenWithNavigation(
                currentRoute = Screen.Products.route,
                navController = navController
            ) {
                PlaceholderScreen("Products")
            }
        }

        composable(Screen.Profile.route) {
            MainScreenWithNavigation(
                currentRoute = Screen.Profile.route,
                navController = navController
            ) {
                ProfilePlaceholderScreen(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = "TODO: Implement $name Screen",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithNavigation(
    currentRoute: String,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Lists") },
                    label = { Text("Lists") },
                    selected = currentRoute == Screen.ShoppingLists.route,
                    onClick = {
                        navController.navigate(Screen.ShoppingLists.route) {
                            popUpTo(Screen.ShoppingLists.route) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Products") },
                    label = { Text("Products") },
                    selected = currentRoute == Screen.Products.route,
                    onClick = {
                        navController.navigate(Screen.Products.route) {
                            popUpTo(Screen.ShoppingLists.route) { inclusive = false }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentRoute == Screen.Profile.route,
                    onClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.ShoppingLists.route) { inclusive = false }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePlaceholderScreen(onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "TODO: Implement Profile Screen",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}