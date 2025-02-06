package hr.tvz.android.movies.views

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.ui.theme.onPrimaryDark
import hr.tvz.android.movies.ui.theme.primaryContainerDark
import hr.tvz.android.movies.data.model.Movie
import hr.tvz.android.movies.view_model.MovieDetailsViewModel
import hr.tvz.android.movies.TopAppBarState
import hr.tvz.android.movies.views.elements.CategoriesLazyRow
import hr.tvz.android.movies.views.elements.LoadingState
import hr.tvz.android.movies.views.elements.MoviesRowSection
import hr.tvz.android.movies.views.elements.TopBarInit


sealed interface MovieDetailsViewState {
    data object Loading: MovieDetailsViewState
    data class Error(val msg: String): MovieDetailsViewState
    data class Success(
        val movie: Movie,
        val recommendedMovies: List<Movie>
    ) : MovieDetailsViewState
}

@Composable
fun MovieDetailsScreen(
    movieDetailsViewModel: MovieDetailsViewModel,
    movieId: Long,
    navController: NavController,
    topAppBarState: MutableState<TopAppBarState>,
) {
    TopBarInit(
        topAppBarState = topAppBarState,
        navController = navController,
        title = "Movie Details",
    )

    val viewModelState by movieDetailsViewModel.viewState.collectAsState()

    LaunchedEffect(key1 = movieDetailsViewModel) {
        movieDetailsViewModel.fetchInitialData(movieId)
    }


    val recommendedMoviesScrollState = rememberLazyListState()

    when(val viewState = viewModelState) {
        is MovieDetailsViewState.Loading -> {
            LoadingState(modifier = Modifier)
        }
        is MovieDetailsViewState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(onPrimaryDark)
            ) {

                // Image, icons
                item {
                    Box(modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                    ) {
                        SubcomposeAsyncImage(
                            model = viewState.movie.posterUrl,
                            contentDescription = "Movie poster image",
                            modifier = Modifier
                                .height(220.dp)
                                .width(220.dp)
                                .align(Alignment.BottomCenter)
                        )
                        Column(modifier = Modifier
                            .fillMaxHeight()
                            .width(58.dp)
                            .align(Alignment.TopEnd)
                        ) {
                            Spacer(modifier = Modifier.height(60.dp))
                            var toggleWatchLaterButton by remember {
                                mutableStateOf(true)
                            }
                            IconButton(
                                onClick = {
                                    /*TODO: Add to user's liked list and change color if added */
                                    toggleWatchLaterButton = !toggleWatchLaterButton
                                },
                                modifier = Modifier
                                    .padding(5.dp),
                                content = {
                                    if (toggleWatchLaterButton) {
                                        Icon(
                                            imageVector = Icons.Default.WatchLater,
                                            contentDescription = "Add to Watch Later list",
                                            tint = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.WatchLater,
                                            contentDescription = "Remove from Watch Later list",
                                            tint = Color.Black
                                        )
                                    }
                                }
                            )
                            var toggleLikeButton by remember {
                                mutableStateOf(true)
                            }
                            IconButton(
                                onClick = {
                                    /*TODO: Add to user's liked list and change color if added */
                                    toggleLikeButton = !toggleLikeButton
                                },
                                modifier = Modifier
                                    .padding(5.dp),
                                content = {
                                    if (toggleLikeButton) {
                                        Icon(
                                            imageVector = Icons.Default.ThumbUp,
                                            contentDescription = "Add to Liked list",
                                            tint = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.ThumbUp,
                                            contentDescription = "Remove from Liked list",
                                            tint = Color.Black
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Title
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text(
                                text = viewState.movie.title,
                                color = Color.White,
                                fontSize = 33.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .basicMarquee() // ToDo: Check how this works
                                    .padding(top = 14.dp)
                            )
                        }
                    )
                }

                //Genres
                item {
                    val genres: List<String> = viewState.movie.genres.split("|").toList()
                    CategoriesLazyRow(
                        categoryList = genres,
                        onCategoryClick = { navController.navigate("movies/category/$it") },
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Release year, duration, rating
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        content = {
                            Column {
                                Text(
                                    text = "Release Year",
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = viewState.movie.releaseYear.toString(),
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "Duration",
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = viewState.movie.lengthMin.toInt().toString() + " min",
                                    color = Color.White)
                            }
                            Column {
                                Text(
                                    text = "Rating",
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating star",
                                        tint = Color.Yellow
                                    )
                                    Text(
                                        text = viewState.movie.imdbRating.toString(),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                }
                            }
                        }
                    )
                }

                // Plot
                item {
                    Text(
                        text = "Plot",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(top = 14.dp, start = 14.dp)
                    )
                    Text(
                        text = viewState.movie.plot,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                //Stars
                item {
                    Column {
                        Text(
                            text = "Stars",
                            color = Color.White,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(top = 14.dp, start = 14.dp)
                        )
                        val stars: Set<String> = viewState.movie.stars
                            .split("|").toSet()
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(all = 14.dp),
                            content = {
                                items(stars.size) {
                                    stars.forEach() {
                                        Text(
                                            text = it,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .background(
                                                    color = primaryContainerDark,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                //Directors
                item {
                    Column {
                        Text(
                            text = "Directors",
                            color = Color.White,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(all = 14.dp)
                        )
                        val directors: Set<String> = viewState.movie.directors
                            .split("|").toSet()
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(all = 14.dp),
                            content = {
                                items(directors.size) {
                                    directors.forEach() {
                                        Text(
                                            text = it,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .background(
                                                    color = primaryContainerDark,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                //Writers
                item {
                    Column {
                        Text(
                            text = "Writers",
                            color = Color.White,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(all = 14.dp)
                        )
                        val writers: Set<String> = viewState.movie.writers
                            .split("|").toSet()
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(all = 14.dp),
                            content = {
                                items(writers.size) {
                                    writers.forEach() {
                                        Text(
                                            text = it,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .background(
                                                    color = primaryContainerDark,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                item {
                    MoviesRowSection(
                        header = "More Like This",
                        lazyListState = recommendedMoviesScrollState,
                        movieList = viewState.recommendedMovies,
                        navController = navController,
                        modifier = Modifier.padding(all = 14.dp)
                    )
                }
            }
        }
        is MovieDetailsViewState.Error -> {
            Text(text = viewState.msg)
        }
    }
}