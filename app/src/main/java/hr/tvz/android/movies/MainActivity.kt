package hr.tvz.android.movies

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hr.tvz.android.movies.ui.theme.primaryLightHighContrast
import dagger.hilt.android.AndroidEntryPoint
import hr.tvz.android.movies.ui.theme.MoviesTheme
import hr.tvz.android.movies.views.CategoryScreen
import hr.tvz.android.movies.views.HomeScreen
import hr.tvz.android.movies.views.LoginScreen
import hr.tvz.android.movies.views.MovieDetailsScreen
import hr.tvz.android.movies.views.RegistrationScreen
import hr.tvz.android.movies.views.SearchScreen
import hr.tvz.android.movies.view_model.AuthViewModel
import hr.tvz.android.movies.view_model.HomeScreenViewModel
import hr.tvz.android.movies.view_model.MovieDetailsViewModel
import hr.tvz.android.movies.view_model.ProfileViewModel
import hr.tvz.android.movies.view_model.SearchViewModel
import hr.tvz.android.movies.views.CreateListScreen
import hr.tvz.android.movies.views.ProfileScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoviesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SideNavDrawer()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun SideNavDrawer() {
        val authViewModel: AuthViewModel = hiltViewModel()
        val navController = rememberNavController()
        val coroutineScope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val topAppBarState = remember { mutableStateOf(TopAppBarState()) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier,
                    content = {
                        val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
                        val context = LocalContext.current

                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = { Text(text = "Home") },
                            selected = false,
                            icon = {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "Home",
                                )
                            },
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                                navController.navigate("movies/home") {
                                    popUpTo(0)
                                }
                            }
                        )

                        HorizontalDivider(thickness = 1.dp)
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    Icons.Default.AccountBox,
                                    contentDescription = "Profile",
                                )
                            },
                            label = { Text(text = "Profile") },
                            selected = false,
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                                navController.navigate("movies/profile") {
                                    popUpTo(0)
                                }
                            }
                        )
                        HorizontalDivider(thickness = 1.dp)
                        if (isUserLoggedIn) {
                            NavigationDrawerItem(
                                label = { Text(text = "Sign out") },
                                selected = false,
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Logout"
                                    ) },
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                    authViewModel.logout(context)
                                    navController.navigate("movies/home") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        } else {
                            NavigationDrawerItem(
                                label = { Text(text = "Sign in") },
                                selected = false,
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Login,
                                        contentDescription = "Login"
                                    ) },
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                    navController.navigate("movies/login") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }
                        HorizontalDivider(thickness = 1.dp)
                    }
                )
            },
            content = {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { topAppBarState.value.title() },
                            navigationIcon = { topAppBarState.value.navigationIcon() },
                            actions = { topAppBarState.value.actions() },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = primaryLightHighContrast,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White
                            )
                        )
                    },
                    content = {
                        MainNavigation(
                            navController = navController,
                            coroutineScope = coroutineScope,
                            drawerState = drawerState,
                            topAppBarState = topAppBarState,
                            authViewModel = authViewModel
                        )
                    }
                )
            }
        )
    }

    @Composable
    private fun MainNavigation(
        authViewModel: AuthViewModel,
        navController: NavHostController,
        coroutineScope: CoroutineScope,
        drawerState: DrawerState,
        topAppBarState: MutableState<TopAppBarState>,
    ) {
        val searchViewModel: SearchViewModel = hiltViewModel()
        val movieDetailsViewModel: MovieDetailsViewModel = hiltViewModel()
        val profileViewModel: ProfileViewModel = hiltViewModel()
        val homeScreenViewModel: HomeScreenViewModel = hiltViewModel()

        NavHost(
            navController = navController,
            startDestination = "movies/home"
        ) {

            composable(route = "movies/home") {
                HomeScreen(
                    homeScreenViewModel = homeScreenViewModel,
                    searchViewModel = searchViewModel,
                    coroutineScope = coroutineScope,
                    drawerState = drawerState,
                    topAppBarState = topAppBarState,
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable(route = "movies/login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    navController = navController,
                )
            }

            composable(route = "movies/register") {
                RegistrationScreen(
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            composable(route = "movies/profile") {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    authViewModel = authViewModel,
                    navController = navController,
                    topAppBarState = topAppBarState,
                    coroutineScope = coroutineScope,
                    drawerState = drawerState
                )
            }

            composable(
                route = "movies/create_list/{username}",
                arguments = listOf(navArgument("username") {
                    type = NavType.StringType
                })
            ) {
                val username: String =
                    it.arguments?.getString("username") ?: ""
                CreateListScreen(
                    profileViewModel = profileViewModel,
                    searchViewModel = searchViewModel,
                    navController = navController,
                    topAppBarState = topAppBarState,
                    username = username
                )
            }

            composable(route = "movies/browse") {
                SearchScreen(navController = navController)
            }

            composable(
                route = "movies/details/{movieId}",
                arguments = listOf(navArgument("movieId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                val movieId: Long =
                    backStackEntry.arguments?.getLong("movieId") ?: -1
                MovieDetailsScreen(
                    movieDetailsViewModel = movieDetailsViewModel,
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    movieId = movieId,
                    navController = navController,
                    topAppBarState = topAppBarState
                )
            }

            composable(
                route = "movies/category/{category_name}",
                arguments = listOf(navArgument("category_name") {
                    type = NavType.StringType
                })
            ) {
                val categoryName: String =
                    it.arguments?.getString("category_name") ?: ""
                CategoryScreen(
                    categoryName = categoryName,
                    navController = navController,
                    topAppBarState = topAppBarState,
                    onMovieClick = { movieId -> navController.navigate("movies/details/"+
                            "$movieId") },
                    onLongClick = { /*ToDo*/}
                    // onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

data class TopAppBarState(
    val title: @Composable () -> Unit = {},
    val navigationIcon: @Composable () -> Unit = {},
    val actions: @Composable () -> Unit = {}
)