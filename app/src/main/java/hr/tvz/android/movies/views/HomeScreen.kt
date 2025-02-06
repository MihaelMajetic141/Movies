@file:JvmName("HomeScreenKt")

package hr.tvz.android.movies.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.ui.theme.onPrimaryDark
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import hr.tvz.android.movies.R
import hr.tvz.android.movies.TopAppBarState
import hr.tvz.android.movies.data.DataStoreManager
import hr.tvz.android.movies.data.model.Movie
import hr.tvz.android.movies.ui.theme.primaryContainerDark
import hr.tvz.android.movies.ui.theme.primaryContainerLightMediumContrast
import hr.tvz.android.movies.ui.theme.primaryLightHighContrast
import hr.tvz.android.movies.view_model.AuthViewModel
import hr.tvz.android.movies.views.elements.CategoriesLazyRow
import hr.tvz.android.movies.views.elements.LoadingState
import hr.tvz.android.movies.views.elements.MoviesRowSection
import hr.tvz.android.movies.views.elements.TopAppBarWithMenuInit
import hr.tvz.android.movies.view_model.HomeScreenViewModel
import hr.tvz.android.movies.view_model.HomeScreenViewState
import hr.tvz.android.movies.view_model.RecommendationsViewState
import hr.tvz.android.movies.view_model.SearchViewModel
import hr.tvz.android.movies.view_model.SearchViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel,
    searchViewModel: SearchViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState,
    topAppBarState: MutableState<TopAppBarState>,
) {
    val homeScreenViewState by homeScreenViewModel.homeScreenViewState.collectAsState()
    val recommendationsViewState by homeScreenViewModel.recommendationsViewState.collectAsState()
    val searchViewState by searchViewModel.searchViewState.collectAsState()
    val context = LocalContext.current
    val dataStore = DataStoreManager(context)
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchInputText by remember { mutableStateOf("") }
    var activeSearch by remember { mutableStateOf(false) }
    val username by dataStore.userName.collectAsState(initial = "")
    val accessToken by dataStore.accessToken.collectAsState(initial = "")
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    TopAppBarWithMenuInit(
        topAppBarState = topAppBarState,
        title = stringResource(id = R.string.app_name),
        navController = navController,
        coroutineScope = coroutineScope,
        drawerState = drawerState,
        authViewModel = authViewModel
    )

    val topRatedScrollState = rememberLazyListState()
    val fetchNextTopRatedPage: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = topRatedScrollState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0
                    && lastVisibleItem?.index == topRatedScrollState.layoutInfo.totalItemsCount - 1
        }
    }
    val newMoviesScrollState = rememberLazyListState()
    val fetchNextNewMoviesPage: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = newMoviesScrollState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0
                    && lastVisibleItem?.index == newMoviesScrollState.layoutInfo.totalItemsCount - 1
        }
    }
    val recommendedMoviesScrollState = rememberLazyListState()
    val fetchNextRecommendedMoviesPage: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = recommendedMoviesScrollState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0
                    && lastVisibleItem?.index == recommendedMoviesScrollState.layoutInfo.totalItemsCount - 1
        }
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

    LaunchedEffect(key1 = Unit) { homeScreenViewModel.fetchInitialData() }

    LaunchedEffect(key1 = fetchNextSearchedMoviePage) {
        if (fetchNextSearchedMoviePage) {
            searchViewModel.fetchNextSearchPage(searchInputText)
        }
    }
    LaunchedEffect(key1 = fetchNextTopRatedPage) {
        if (fetchNextTopRatedPage) {
            homeScreenViewModel.fetchNextTopRatedPage()
        }
    }
    LaunchedEffect(key1 = fetchNextNewMoviesPage) {
        if (fetchNextNewMoviesPage) {
            homeScreenViewModel.fetchNextNewMoviesPage()
        }
    }

    LaunchedEffect(key1 = accessToken) {
        username?.let { accessToken?.let { it1 ->
            homeScreenViewModel.fetchRecommendedMovies(it,
                it1
            )
        } }
    }

    when (val viewState = homeScreenViewState) {
        is HomeScreenViewState.Loading -> LoadingState(Modifier)
        is HomeScreenViewState.Success -> {
            LaunchedEffect(key1 = fetchNextRecommendedMoviesPage) {
                if (fetchNextRecommendedMoviesPage) {
                    username?.let { accessToken?.let { it1 ->
                        homeScreenViewModel.fetchMoreRecommendedMovies(it,
                            it1
                        )
                    } }
                }
            }

            Column(
                Modifier
                    .padding(vertical = 30.dp)
                    .background(onPrimaryDark)) {
                Spacer(modifier = Modifier.height(40.dp))
                SearchBar(
                    query = searchInputText,
                    onQueryChange = { searchInputText = it},
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
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    content = {
                        when(searchViewState) {
                            is SearchViewState.Loading -> {
                                CircularProgressIndicator()
                            }

                            is SearchViewState.SearchResultsFetched -> {
                                if ((searchViewState as SearchViewState.SearchResultsFetched)
                                        .searchResults.isEmpty()) { MovieListEmptyState() }
                                else {
                                    SearchedMoviesLazyColumn(
                                        movies = (searchViewState as SearchViewState
                                            .SearchResultsFetched).searchResults,
                                        lazyListState = searchScrollState,
                                        navController = navController,
                                        modifier = Modifier.height(300.dp)
                                    )
                                }
                            }

                            is SearchViewState.Error -> {
                                Text(text = (searchViewState as SearchViewState.Error).msg)
                            }

                            SearchViewState.Idle -> {}
                        }
                    }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // item { ImageSlider(viewState.topRatedMoviesList) }
                    item {
                        CategoriesLazyRow(
                            categoryList = allCategoriesList,
                            onCategoryClick = {
                                navController.navigate("movies/category/$it") },
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        MoviesRowSection(
                            header = "New Movies",
                            movieList = viewState.newMoviesList,
                            lazyListState = newMoviesScrollState,
                            navController = navController
                        )
                    }

                    item {
                        MoviesRowSection(
                            header = "Top Rated",
                            movieList = viewState.topRatedMoviesList,
                            lazyListState = topRatedScrollState,
                            navController = navController,
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        when(recommendationsViewState){
                            is RecommendationsViewState.Error -> {}
                            is RecommendationsViewState.Loading -> {
                                LoadingState(modifier = Modifier.padding(10.dp))
                            }
                            RecommendationsViewState.Empty -> {
                                Text(
                                    text = "Recommended",
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(5.dp)
                                )
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .padding(5.dp)
                                    .background(color = primaryLightHighContrast),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isUserLoggedIn) {
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
                                                text = "Not Signed In",
                                                color = Color.White,
                                                fontSize = 20.sp
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Sign In in order to get suitable recommendations",
                                                color = Color.LightGray,
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { navController.navigate(
                                                    route = "movies/login") },
                                                colors = ButtonDefaults.buttonColors(primaryContainerDark)
                                            ) {
                                                Text(text = "Sign In", color = Color.LightGray)
                                            }
                                        }
                                    }
                                }
                            }

                            is RecommendationsViewState.Success -> {
                                Text(
                                    text = "Recommended",
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(5.dp)
                                )
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(281.dp)
                                    .padding(5.dp)
                                    .background(color = primaryLightHighContrast),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (username?.equals("") == false
                                        && (recommendationsViewState as RecommendationsViewState.Success)
                                            .recommendedMoviesList.isNotEmpty()) {
                                        MoviesRowSection(
                                            header = "Recommended for you",
                                            movieList = (recommendationsViewState as RecommendationsViewState.Success)
                                                .recommendedMoviesList,
                                            lazyListState = recommendedMoviesScrollState,
                                            navController = navController
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                    }

                }
            }
        }

        is HomeScreenViewState.Error -> Text(text = "Error")
    }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun ImageSlider(
    movieList: List<Movie>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(0)
    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(7000)
            pagerState.animateScrollToPage(
                page = (pagerState.currentPage + 1) % (pagerState.pageCount)
            )
        }
    }
    Column(Modifier.height(200.dp)) {
        HorizontalPager(
            count = movieList.size,
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = modifier
                .fillMaxSize()
        ) { page ->
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue

                        lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        ).also { scale ->
                            scaleX = scale
                            scaleY = scale
                        }

                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            ) {
                SubcomposeAsyncImage(
                    model = movieList[page].posterUrl,
                    contentDescription = stringResource(R.string.image_slider),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )
    }
}