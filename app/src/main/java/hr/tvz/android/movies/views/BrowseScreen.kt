package hr.tvz.android.movies.views

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.data.model.Movie
import hr.tvz.android.movies.view_model.SearchViewModel
import hr.tvz.android.movies.view_model.SearchViewState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val viewState = viewModel.searchViewState.collectAsState().value
    val keyboardController = LocalSoftwareKeyboardController.current
    // ToDo: reset input before leaving this screen
    var inputText by remember {
        mutableStateOf("")
    }
    var active by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        SearchBar(
            query = inputText,
            onQueryChange = { inputText = it},
            onSearch = {
                viewModel.search(inputText)
                keyboardController?.hide()
            },
            active = active,
            onActiveChange = {
                active = it
                inputText = ""
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (active) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.clickable {
                            if (inputText.isNotEmpty()) {
                                inputText = ""
                            } else {
                                active = false
                            }
                        }
                    )
                }
            },
            content = {
                when(viewState) {
                    is SearchViewState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is SearchViewState.SearchResultsFetched -> {
                        if (viewState.searchResults.isEmpty()) {
                            MovieListEmptyState()
                        } else {
                            val lazyListScrollState = rememberLazyListState()
                            val fetchNextSearchedMoviePage: Boolean by remember {
                                derivedStateOf {
                                    val lastVisibleItem = lazyListScrollState.layoutInfo.visibleItemsInfo.lastOrNull()
                                    lastVisibleItem?.index != 0
                                            && lastVisibleItem?.index == lazyListScrollState.layoutInfo.totalItemsCount - 1
                                }
                            }

                            LaunchedEffect(key1 = fetchNextSearchedMoviePage) {
                                if (fetchNextSearchedMoviePage) {
                                    viewModel.fetchNextSearchPage(inputText)
                                }
                            }

                            SearchedMoviesLazyColumn(
                                movies = viewState.searchResults,
                                lazyListState = lazyListScrollState,
                                navController = navController,
                            )
                        }
                    }

                    is SearchViewState.Error -> {
                        Text(text = viewState.msg)
                    }

                    SearchViewState.Idle -> {}
                }
            }
        )
    }
}

@Composable
fun SearchedMoviesLazyColumn(
    movies: List<Movie>,
    lazyListState: LazyListState,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        content = {
            movies.forEach() {
                item {
                    MovieListItem(
                        movie = it,
                        modifier = Modifier
                            .clickable {
                                navController.navigate("movies/details/${it.id}")
                            }
                    )
                }
            }
        })
}

@Composable
fun MovieListItem(
    movie: Movie,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
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
            modifier = Modifier.padding(8.dp)
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
    }
}

@Composable
fun MovieListEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "No movies found",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "Try adjusting your search",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}



