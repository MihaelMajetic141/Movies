package hr.tvz.android.movies.views.elements


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.TopAppBarState
import hr.tvz.android.movies.data.DataStoreManager
import hr.tvz.android.movies.view_model.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TopBarInit(
    topAppBarState: MutableState<TopAppBarState>,
    navController: NavController,
    title: String,
) {
    topAppBarState.value = TopAppBarState(
        title = { Text(text = title, fontSize = 26.sp) },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack()
                },
                content = {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back Button")
                }
            )
        }
    )
}

@Composable
fun TopAppBarWithMenuInit(
    authViewModel: AuthViewModel,
    topAppBarState: MutableState<TopAppBarState>,
    title: String,
    navController: NavController,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState,
) {
    // val loggedInState by rememberUpdatedState(authViewModel.isLoggedIn.collectAsState())
    // val dataStoreManager = DataStoreManager(context)

    topAppBarState.value = TopAppBarState(
        title = { Text(text = title, fontSize = 26.sp) },
        navigationIcon = {
            IconButton (
                onClick = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                },
                content = {
                    Icon(Icons.Default.Menu, contentDescription = "Menu Button")
                }
            )
        },
        actions = {
            val context = LocalContext.current
            val dataStoreManager = DataStoreManager(context)
            val userPicture by dataStoreManager.userPicture.collectAsState(initial = "")
            val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

            if (isUserLoggedIn && !userPicture.equals("")) {
                SubcomposeAsyncImage(
                    model = userPicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .padding(4.dp)
                        .size(52.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .clickable { navController.navigate("movies/profile") }
                )
            } else {
                IconButton(
                    onClick = {
                        navController.navigate("movies/profile") { popUpTo(0) }
                    },
                    content = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(52.dp)
                        )
                    }
                )
            }
            /*
            DropdownMenu(
                expanded = showDropDownMenu,
                onDismissRequest = { showDropDownMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Profile") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Profile") },
                    onClick = {
                        navController.navigate("user/profile") { popUpTo(0) }
                        showDropDownMenu = false
                    }
                )
            }
             */
        }
    )


    /*
    when(loggedInState.value) {
        true -> {

        }
        false -> {
            topAppBarState.value = TopAppBarState(
                title = { Text(text = title, fontSize = 26.sp) },
                navigationIcon = {
                    IconButton (
                        onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        content = {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Button")
                        }
                    )
                },
                actions = {
                    var showDropDownMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showDropDownMenu = !showDropDownMenu },
                        content = {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Account",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(52.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sign in") },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Login,
                                    contentDescription = "Sign in") },
                            onClick = {
                                navController.navigate("movies/login") { popUpTo(0) }
                                showDropDownMenu = false
                            }
                        )
                    }
                }
            )
        }
    }
    */


    /*
    when (loggedInState.value) {
         true -> {
            topAppBarState.value = TopAppBarState(
                title = { Text(text = title, fontSize = 26.sp) },
                navigationIcon = {
                    IconButton (
                        onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        content = {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Button")
                        }
                    )
                },
                actions = {
                    var showDropDownMenu by remember { mutableStateOf(false) }
                    SubcomposeAsyncImage(
                        model = dataStoreManager.userPicture.collectAsState(initial = ""),
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(52.dp)
                            .clickable { showDropDownMenu = !showDropDownMenu }
                    )
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AccountBox,
                                    contentDescription = "Profile") },
                            onClick = { navController.navigate("user/profile") }
                        )
                        DropdownMenuItem(
                            text = { Text("Sign out") },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Log out") },
                            onClick = {
                                authViewModel.logout(context)
                                showDropDownMenu = false
                                Toast.makeText(context, "Logged out", Toast.LENGTH_LONG).show()
                                navController.navigate("movies/home") { popUpTo(0) }
                            }
                        )
                    }
                }
            )
        }
    }
    */


}
