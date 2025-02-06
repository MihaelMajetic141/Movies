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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.TopAppBarState
import hr.tvz.android.movies.data.DataStoreManager
import hr.tvz.android.movies.ui.theme.onPrimaryDark
import hr.tvz.android.movies.ui.theme.primaryContainerDark
import hr.tvz.android.movies.ui.theme.primaryContainerLightMediumContrast
import hr.tvz.android.movies.ui.theme.primaryLightHighContrast
import hr.tvz.android.movies.view_model.ProfileViewModel
import hr.tvz.android.movies.view_model.ProfileViewState
import hr.tvz.android.movies.view_model.SearchViewModel
import hr.tvz.android.movies.view_model.SearchViewState
import hr.tvz.android.movies.views.elements.LoadingState
import hr.tvz.android.movies.views.elements.TopBarInit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListScreen(
    profileViewModel: ProfileViewModel,
    searchViewModel: SearchViewModel,
    navController: NavController,
    topAppBarState: MutableState<TopAppBarState>,
    username: String
) {
    val searchViewState by searchViewModel.searchViewState.collectAsState()
    val profileViewState by profileViewModel.profileViewState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val accessToken by dataStoreManager.accessToken.collectAsState(initial = "")
    val configuration = LocalConfiguration.current
    var searchInputText by remember { mutableStateOf("") }
    var activeSearch by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = (accessToken != "")) {
        accessToken?.let { profileViewModel.fetchInitialData(username, it) }
    }

    val searchScrollState = rememberLazyListState()
    val fetchNextSearchedMoviePage: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = searchScrollState
                .layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0
                    && lastVisibleItem?.index ==
                    searchScrollState
                        .layoutInfo.totalItemsCount - 1
        }
    }
    LaunchedEffect(key1 = fetchNextSearchedMoviePage) {
        searchViewModel.fetchNextSearchPage(searchInputText)
    }
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {

        }
        else -> {

        }
    }

    TopBarInit(topAppBarState = topAppBarState, navController = navController, title = "User Lists")
    Column(
        Modifier
            .background(onPrimaryDark)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(75.dp))
        SearchBar(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(10.dp),
            query = searchInputText,
            onQueryChange = { searchInputText = it },
            onSearch = {
                searchViewModel.search(searchInputText)
                keyboardController?.hide()
            },
            active = activeSearch,
            onActiveChange = { activeSearch = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (activeSearch) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.clickable {
                            if (searchInputText.isNotEmpty()) {
                                searchInputText = ""
                            } else {
                                activeSearch = false
                            }
                        }
                    )
                }
            },
            content = {
                when (searchViewState) {
                    is SearchViewState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is SearchViewState.SearchResultsFetched -> {
                        if ((searchViewState as SearchViewState.SearchResultsFetched)
                                .searchResults.isEmpty()
                        ) {
                            MovieListEmptyState()
                        } else {
                            LazyColumn(
                                state = searchScrollState,
                            ) {
                                val movies = (searchViewState as SearchViewState
                                .SearchResultsFetched)
                                    .searchResults
                                items(movies) { movie ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = movie.posterUrl,
                                            contentDescription = "Movie poster",
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(3.dp)
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)  // Takes all available space between poster and icons
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = movie.title,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                modifier = Modifier.basicMarquee()
                                            )
                                            Text(
                                                text = movie.releaseYear.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    accessToken?.let {
                                                        profileViewModel
                                                            .addMovieToWatchList(
                                                                context,
                                                                movie.id,
                                                                username,
                                                                accessToken = it
                                                            )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.WatchLater,
                                                    contentDescription = "Add icon",
                                                    tint = primaryContainerLightMediumContrast,
                                                    modifier = Modifier.height(33.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    accessToken?.let {
                                                        profileViewModel
                                                            .addMovieToFavorites(
                                                                context,
                                                                movie.id,
                                                                username,
                                                                it
                                                            )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircleOutline,
                                                    contentDescription = "Add icon",
                                                    tint = primaryContainerLightMediumContrast,
                                                    modifier = Modifier.height(33.dp)
                                                )
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }

                    is SearchViewState.Error -> {
                        Text(text = (searchViewState as SearchViewState.Error).msg)
                    }

                    SearchViewState.Idle -> {}
                }
            }
        )

        when(val viewState = profileViewState){
            is ProfileViewState.Loading -> {
                LoadingState(modifier = Modifier.fillMaxWidth())
            }
            is ProfileViewState.Success -> {
                LazyColumn {
                    item {
                        Text(
                            text = "Watchlist",
                            fontSize = 24.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(10.dp)
                        )
                        val columnHeight: Int = if (viewState.watchList.count() < 5) {
                            viewState.watchList.count() * 80
                        } else {
                            400
                        }

                        if (viewState.watchList.isEmpty()) {
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
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(primaryContainerDark)
                                ) {
                                    Text(text = "Create a watchlist", color = Color.LightGray)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                        }
                        else {
                            LazyColumn(
                                Modifier
                                    .fillMaxWidth()
                                    .height(columnHeight.dp)
                                    .background(primaryLightHighContrast)
                            ) {
                                items(viewState.watchList) { movie ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        Column(modifier = Modifier
                                            .padding(start = 8.dp)
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = movie.posterUrl,
                                                contentDescription = "Movie poster",
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .padding(3.dp)
                                            )
                                        }
                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .weight(1f)
                                        ) {
                                            Text(text = movie.title,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier
                                                    .basicMarquee()
                                            )
                                            Text(text = movie.releaseYear.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                        Column {
                                            IconButton(
                                                onClick = {
                                                    accessToken?.let {
                                                        profileViewModel
                                                            .removeMovieFromWatchList(
                                                                context, movie.id, username, it
                                                            )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove icon",
                                                    tint = primaryContainerLightMediumContrast,
                                                    modifier = Modifier.height(33.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text(text = "Favorites",
                            fontSize = 24.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(10.dp)
                        )
                        val columnHeight: Int = if (viewState.likedList.count() < 5) {
                            viewState.likedList.count() * 80
                        } else {
                            400
                        }

                        if (viewState.likedList.isEmpty()) {
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
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(primaryContainerDark)
                                ) {
                                    Text(text = "Create Favorites list", color = Color.LightGray)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(columnHeight.dp)
                                    .background(primaryLightHighContrast)
                            ) {
                                items(viewState.likedList) { movie ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)) {
                                        Column(Modifier.padding(start = 8.dp)) {
                                            SubcomposeAsyncImage(
                                                model = movie.posterUrl,
                                                contentDescription = "Movie poster",
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .padding(3.dp)
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.Start,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .weight(1f)
                                        ) {
                                            Text(text = movie.title,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier
                                                    .basicMarquee()
                                            )
                                            Text(text = movie.releaseYear.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                        Column {
                                            IconButton(
                                                onClick = {
                                                    accessToken?.let {
                                                        profileViewModel
                                                            .removeMovieFromFavorites(
                                                                context, movie.id, username, it
                                                            )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove icon",
                                                    tint = primaryContainerLightMediumContrast,
                                                    modifier = Modifier.height(33.dp)
                                                )
                                            }
                                        }
                                    }
                                }


                            }
                        }
                    }
                }
            }
            is ProfileViewState.Error -> {}
        }
    }

}