package hr.tvz.android.movies.views

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.TopAppBarState
import hr.tvz.android.movies.data.DataStoreManager
import hr.tvz.android.movies.ui.theme.onPrimaryDark
import hr.tvz.android.movies.ui.theme.primaryContainerDark
import hr.tvz.android.movies.ui.theme.primaryContainerLightMediumContrast
import hr.tvz.android.movies.ui.theme.primaryLightHighContrast
import hr.tvz.android.movies.view_model.AuthViewModel
import hr.tvz.android.movies.view_model.ProfileViewState
import hr.tvz.android.movies.view_model.ProfileViewModel
import hr.tvz.android.movies.view_model.RecommendationsViewState
import hr.tvz.android.movies.view_model.SearchViewModel
import hr.tvz.android.movies.view_model.SearchViewState
import hr.tvz.android.movies.views.elements.LoadingState
import hr.tvz.android.movies.views.elements.MovieCard
import hr.tvz.android.movies.views.elements.TopAppBarWithMenuInit
import hr.tvz.android.movies.views.elements.TopBarInit
import kotlinx.coroutines.CoroutineScope


@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    topAppBarState: MutableState<TopAppBarState>,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState
) {
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    when (isUserLoggedIn){
        true -> {
            UserProfileScreen(
                profileViewModel = profileViewModel,
                navController = navController,
                authViewModel = authViewModel,
                topAppBarState = topAppBarState,
                coroutineScope = coroutineScope,
                drawerState = drawerState
            )
        }
        false -> {
            NoUserProfileScreen(
                navController = navController
            )
        }
    }
}

@Composable
fun UserProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    topAppBarState: MutableState<TopAppBarState>,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState
) {
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val accessToken by dataStoreManager.accessToken.collectAsState(initial = "")
    val email by dataStoreManager.userEmail.collectAsState(initial = "")
    val username by dataStoreManager.userName.collectAsState(initial = "")
    val userPicture by dataStoreManager.userPicture.collectAsState(initial = "")
    val profileViewState by profileViewModel.profileViewState.collectAsState()
    val recommendationsViewState by profileViewModel.recommendationsViewState.collectAsState()

    LaunchedEffect(key1 = accessToken) {
        username?.let { accessToken?.let { it1 -> profileViewModel.fetchInitialData(it, it1) } }
    }

    val recommendedMoviesScrollState = rememberLazyListState()
    val fetchNextRecommendedMoviesPage: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = recommendedMoviesScrollState.layoutInfo
                .visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0
                    && lastVisibleItem?.index == recommendedMoviesScrollState
                .layoutInfo.totalItemsCount - 1
        }
    }
    LaunchedEffect(key1 = fetchNextRecommendedMoviesPage) {
        profileViewModel.fetchMoreRecommendations()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = onPrimaryDark),
        content = {
            item {
                TopAppBarWithMenuInit(
                    authViewModel = authViewModel,
                    topAppBarState = topAppBarState,
                    navController = navController,
                    title = "Profile",
                    coroutineScope = coroutineScope,
                    drawerState = drawerState
                )
            }
            item {
                Spacer(modifier = Modifier.height(60.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                    contentAlignment = Alignment.Center) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (userPicture.equals("")) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile picture",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .height(58.dp)
                                    .width(58.dp)
                                    .clip(RoundedCornerShape(58.dp))
                            )
                        } else {
                            SubcomposeAsyncImage(
                                model = userPicture,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .height(58.dp)
                                    .width(58.dp)
                                    .clip(RoundedCornerShape(58.dp))
                            )
                        }
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)) {
                            username?.let {
                                Text(text = it,
                                    fontSize = 16.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(start = 16.dp, top = 10.dp)
                                )
                            }
                            email?.let {
                                Text(text = it,
                                    fontSize = 12.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(start = 16.dp, top = 10.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier
                            .height(58.dp)
                            .width(58.dp)
                        ) {
                            IconButton(
                                onClick = { /*ToDo: Open SettingsScreen */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint= Color.LightGray,
                                    modifier = Modifier
                                        .height(88.dp)
                                        .width(88.dp)
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically , modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(5.dp)
                ) {
                    Button(
                        onClick = {
                            authViewModel.logout(context)
                            navController.navigate("movies/profile") },
                        shape = ButtonDefaults.elevatedShape,
                        colors = ButtonDefaults.buttonColors(primaryContainerDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        Text(
                            text = "Sign out", fontSize = 20.sp,
                            color = Color.LightGray)
                    }
                }
            }
            item {
                Row(modifier = Modifier
                    .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Watchlist",
                            fontSize = 24.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column {
                        Text(text = "Edit Watchlist",
                            color = primaryContainerLightMediumContrast,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(10.dp)
                                .clickable { navController.navigate("movies/create_list/$username") }
                        )
                    }

                }
                when(val viewState = profileViewState) {
                    is ProfileViewState.Loading -> {
                        LoadingState(modifier = Modifier.fillMaxWidth())
                    }
                    is ProfileViewState.Success -> {
                        var showDropDownMenu by remember { mutableStateOf(false) }
                        var selectedMovieId by remember { mutableLongStateOf(0L) }
                        if (viewState.watchList.isNotEmpty()){
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(282.dp)
                                .padding(5.dp)
                                .background(color = primaryLightHighContrast),
                                contentAlignment = Alignment.Center
                            ) {
                                LazyRow {
                                    items(viewState.watchList) { movie ->
                                        MovieCard(
                                            movie = movie,
                                            onLongClick = {
                                                showDropDownMenu = true
                                                selectedMovieId = movie.id },
                                            onClick = { movieId ->
                                                navController
                                                    .navigate("movies/details/$movieId")
                                            },
                                            modifier = Modifier
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showDropDownMenu,
                                    onDismissRequest = { showDropDownMenu = false }) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Remove icon",
                                                tint = primaryContainerLightMediumContrast,
                                                modifier = Modifier.height(33.dp)
                                            )
                                        },
                                        text = { Text("Remove from Watchlist") },
                                        onClick = {
                                            username?.let { username ->
                                                accessToken?.let { token ->
                                                    profileViewModel.removeMovieFromWatchList(
                                                        context, selectedMovieId, username, token
                                                    )
                                                }
                                            }
                                            showDropDownMenu = false
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "Add icon",
                                    tint = primaryContainerLightMediumContrast,
                                    modifier = Modifier.height(33.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "No Watchlist yet",
                                    color = Color.White,
                                    fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Create a Watchlist to keep track of shows and movies "
                                        + "you want to watch",
                                    color = Color.LightGray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(10.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { navController.navigate("movies/create_list/$username") },
                                    colors = ButtonDefaults.buttonColors(primaryContainerDark)
                                ) {
                                    Text(text = "Create a watchlist", color = Color.LightGray)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    is ProfileViewState.Error -> {
                        Text(
                            text = "Error: ${viewState.msg}",
                            fontSize = 24.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
            item {
                Row {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Favorites",
                            fontSize = 24.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column {
                        Text(text = "Edit Favorites",
                            color = primaryContainerLightMediumContrast,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(10.dp)
                                .clickable { navController.navigate("movies/create_list/$username") }
                        )
                    }
                }
                when (val viewState = profileViewState) {
                    is ProfileViewState.Loading -> {
                        LoadingState(modifier = Modifier.fillMaxWidth())
                    }
                    is ProfileViewState.Success -> {
                        LaunchedEffect(key1 = Unit) {
                            profileViewModel.fetchInitialRecommendations()
                        }
                        if (viewState.likedList.isNotEmpty()){
                            var showDropDownMenu by remember { mutableStateOf(false) }
                            var selectedMovieId by remember { mutableLongStateOf(0L) }
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(282.dp)
                                .padding(5.dp)
                                .background(color = primaryLightHighContrast),
                                contentAlignment = Alignment.Center
                            ) {
                                LazyRow {
                                    items(viewState.likedList) { movie ->
                                        MovieCard(
                                            movie = movie,
                                            onLongClick = {
                                                showDropDownMenu = true
                                                selectedMovieId = movie.id },
                                            onClick = { movieId ->
                                                navController
                                                    .navigate("movies/details/$movieId")
                                            },
                                            modifier = Modifier
                                        )
                                        DropdownMenu(
                                            expanded = showDropDownMenu,
                                            onDismissRequest = { showDropDownMenu = false }) {
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Remove icon",
                                                        tint = primaryContainerLightMediumContrast,
                                                        modifier = Modifier.height(33.dp)
                                                    )
                                                },
                                                text = { Text("Remove from Favorites") },
                                                onClick = {
                                                    username?.let { username ->
                                                        accessToken?.let { token ->
                                                            profileViewModel.removeMovieFromFavorites(
                                                                context, selectedMovieId, username, token
                                                            )
                                                        }
                                                    }
                                                    showDropDownMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "Add icon",
                                    tint = primaryContainerLightMediumContrast,
                                    modifier = Modifier.height(33.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Favorites yet",
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Create Favorites list to get suitable recommendations",
                                    color = Color.LightGray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(10.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { navController.navigate("movies/create_list/$username") },
                                    colors = ButtonDefaults.buttonColors(primaryContainerDark)
                                ) {
                                    Text(text = "Create Favorites list", color = Color.LightGray)
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    is ProfileViewState.Error -> {
                        Text(
                            text = "Error: ${viewState.msg}",
                            fontSize = 24.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
            item {
                Text(
                    text = "Recommended",
                    fontSize = 24.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(10.dp)
                )
                when (val viewState = recommendationsViewState){
                    is RecommendationsViewState.Loading -> {
                        LoadingState(modifier = Modifier.fillMaxWidth())
                    }
                    is RecommendationsViewState.Success -> {
                        if (viewState.recommendedMoviesList.isNotEmpty()){
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(282.dp)
                                .padding(5.dp)
                                .background(color = primaryLightHighContrast),
                                contentAlignment = Alignment.Center
                            ) {
                                LazyRow(
                                    state = recommendedMoviesScrollState
                                ) {
                                    items(viewState.recommendedMoviesList) { movie ->
                                        MovieCard(
                                            movie = movie,
                                            onLongClick = { }, // ToDo: Handle long click
                                            onClick = { movieId ->
                                                navController
                                                    .navigate("movies/details/$movieId")
                                            },
                                            modifier = Modifier
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                    is RecommendationsViewState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Add icon",
                                tint = primaryContainerLightMediumContrast,
                                modifier = Modifier.height(33.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Recommendations not available",
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Add movies to Favorites in order to get suitable recommendations",
                                color = Color.LightGray,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { navController.navigate("movies/create_list/$username") },
                                colors = ButtonDefaults.buttonColors(primaryContainerDark)
                            ) {
                                Text(text = "Create list", color = Color.LightGray)
                            }
                        }
                    }

                    RecommendationsViewState.Empty -> {}
                }
            }
        }
    )
}

@Composable
fun NoUserProfileScreen(
    navController: NavController,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = onPrimaryDark),
        content = {
            item {
                Spacer(modifier = Modifier.height(75.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                    contentAlignment = Alignment.Center) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile picture",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .height(58.dp)
                                .width(58.dp)
                        )
                        Text(text = "Sign In",
                            fontSize = 20.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Column(modifier = Modifier
                        .align(Alignment.TopEnd)
                        .height(88.dp)
                        .width(88.dp)
                        .padding(24.dp)
                    ) {
                        IconButton(
                            // ToDo: Navigate to SettingsScreen
                            onClick = { navController.navigate("user/settings") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint= Color.LightGray,
                                modifier = Modifier
                                    .height(88.dp)
                                    .width(88.dp)
                            )
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically , modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(5.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("movies/login") },
                        shape = ButtonDefaults.elevatedShape,
                        colors = ButtonDefaults.buttonColors(primaryContainerDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        Text(text = "Sign In / Sign Up", fontSize = 20.sp, color = Color.LightGray)
                    }
                }
            }
            item {
                Text(
                    text = "Watchlist",
                    fontSize = 24.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(10.dp)
                )
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(5.dp)
                    .background(color = primaryLightHighContrast),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add to watchlist icon",
                            tint = primaryContainerLightMediumContrast,
                            modifier = Modifier.height(33.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Sign in to access your Watchlist",
                            color = Color.White,
                            fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Save shows and movies to keep track of what you want to watch",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { navController.navigate("movies/login") },
                            colors = ButtonDefaults.buttonColors(primaryContainerDark)
                        ) {
                            Text(text = "Sign In", color = Color.LightGray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                Text(
                    text = "Favorite Movies",
                    fontSize = 24.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(10.dp)
                )
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(5.dp)
                    .background(color = primaryLightHighContrast),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add to favorites icon",
                            tint = primaryContainerLightMediumContrast,
                            modifier = Modifier.height(33.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Sign in to access your Favorite list",
                            color = Color.White,
                            fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Save shows and movies to Favorits in order to get " +
                                "suitable recommendations",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { navController.navigate("movies/login") },
                            colors = ButtonDefaults.buttonColors(primaryContainerDark)
                        ) {
                            Text(text = "Sign In", color = Color.LightGray)
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Recommended",
                    fontSize = 24.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(10.dp)
                )
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(5.dp)
                    .background(color = primaryLightHighContrast),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add icon",
                            tint = primaryContainerLightMediumContrast,
                            modifier = Modifier.height(33.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Sign in to access your Recommendations list",
                            color = Color.White,
                            fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Acquire recommendations based on your favorite movie " +
                                "preferences",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { navController.navigate("user/login") },
                            colors = ButtonDefaults.buttonColors(primaryContainerDark)
                        ) {
                            Text(text = "Sign In", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MockProfileScreenPreview(){
    NoUserProfileScreen(navController = rememberNavController())
}