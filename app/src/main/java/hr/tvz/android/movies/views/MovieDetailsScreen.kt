package hr.tvz.android.movies.views

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import hr.tvz.android.movies.data.DataStoreManager
import hr.tvz.android.movies.view_model.AuthViewModel
import hr.tvz.android.movies.view_model.ProfileViewModel
import hr.tvz.android.movies.views.elements.TextLazyRow
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
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    movieId: Long,
    navController: NavController,
    topAppBarState: MutableState<TopAppBarState>,
) {
    TopBarInit(
        topAppBarState = topAppBarState,
        navController = navController,
        title = "Movie Details",
    )

    val movieDetailsViewState by movieDetailsViewModel.viewState.collectAsState()

    LaunchedEffect(key1 = movieDetailsViewModel) {
        movieDetailsViewModel.fetchInitialData(movieId)
    }

    when (val viewState = movieDetailsViewState) {
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
                    val isUserLoggedIn by authViewModel
                        .isUserLoggedIn.collectAsState()
                    val isMovieInWatchList by movieDetailsViewModel
                        .isMovieInWatchList.collectAsState()
                    val isMovieInFavorites by movieDetailsViewModel
                        .isMovieInFavorites.collectAsState()
                    val context = LocalContext.current
                    val dataStore = DataStoreManager(LocalContext.current)
                    val username by dataStore.userName.collectAsState(initial = "")
                    val accessToken by dataStore.accessToken.collectAsState(initial = "")

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
                            var showLoginPopUp by remember { mutableStateOf(false) }

                            Spacer(modifier = Modifier.height(60.dp))
                            if (isUserLoggedIn) {
                                LaunchedEffect(key1 = username) {
                                    username?.let {
                                        accessToken?.let { it1 ->
                                            movieDetailsViewModel.checkIfMovieIsInWatchList(movieId,
                                                it, it1
                                            )
                                        }
                                    }
                                }
                                LaunchedEffect(key1 = username) {
                                    username?.let {
                                        accessToken?.let { it1 ->
                                            movieDetailsViewModel.checkIfMovieIsInFavorites(movieId,
                                                it, it1
                                            )
                                        }
                                    }
                                }
                                when (isMovieInWatchList) {
                                    true -> {
                                        IconButton(
                                            onClick = {
                                                username?.let { username ->
                                                    accessToken?.let { token ->
                                                        profileViewModel.removeMovieFromWatchList(
                                                            movieId = movieId,
                                                            context = context,
                                                            username = username,
                                                            accessToken = token
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(5.dp),
                                            content = {
                                                Icon(
                                                    imageVector = Icons.Default.WatchLater,
                                                    contentDescription = "Remove from Watchlist",
                                                    tint = Color.White
                                                )
                                            }
                                        )
                                    }
                                    false -> {
                                        IconButton(
                                            onClick = {
                                                username?.let { username ->
                                                    accessToken?.let { token ->
                                                        profileViewModel.addMovieToWatchList(
                                                            movieId = movieId,
                                                            context = context,
                                                            username = username,
                                                            accessToken = token
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(5.dp),
                                            content = {
                                                Icon(
                                                    imageVector = Icons.Default.WatchLater,
                                                    contentDescription = "Add to Watchlist",
                                                    tint = Color.Black
                                                )
                                            }
                                        )
                                    }
                                }
                                when (isMovieInFavorites) {
                                    true -> {
                                        IconButton(
                                            onClick = {
                                                username?.let { username ->
                                                    accessToken?.let { token ->
                                                        profileViewModel.removeMovieFromFavorites(
                                                            movieId = movieId,
                                                            context = context,
                                                            username = username,
                                                            accessToken = token
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(5.dp),
                                            content = {
                                                Icon(
                                                    imageVector = Icons.Default.ThumbUp,
                                                    contentDescription = "Remove from Favorites",
                                                    tint = Color.White
                                                )
                                            }
                                        )
                                    }
                                    false -> {
                                        IconButton(
                                            onClick = {
                                                username?.let { username ->
                                                    accessToken?.let { token ->
                                                        profileViewModel.addMovieToFavorites(
                                                            movieId = movieId,
                                                            context = context,
                                                            username = username,
                                                            accessToken = token
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(5.dp),
                                            content = {
                                                Icon(
                                                    imageVector = Icons.Default.ThumbUp,
                                                    contentDescription = "Add to Favorites",
                                                    tint = Color.Black
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            else {
                                if (showLoginPopUp){
                                    AlertDialogExample(
                                        onDismissRequest = { showLoginPopUp = false },
                                        onConfirmation = {
                                            navController.navigate("movies/login")
                                            showLoginPopUp = false
                                        },
                                        dialogTitle = "Login required",
                                        dialogText = "You need to be logged in to add movies to lists.",
                                        icon = Icons.AutoMirrored.Filled.Login
                                    )
                                }
                                IconButton(
                                    onClick = { showLoginPopUp = true},
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.WatchLater,
                                            contentDescription = "Add to Watch Later list",
                                            tint = Color.White
                                        )
                                    },
                                    modifier = Modifier.padding(5.dp)
                                )
                                IconButton(
                                    onClick = { showLoginPopUp = true },
                                    modifier = Modifier
                                        .padding(5.dp),
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.ThumbUp,
                                            contentDescription = "Add to Liked list",
                                            tint = Color.White
                                        )
                                    }
                                )
                            }
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
                                    .basicMarquee()
                                    .padding(top = 14.dp)
                            )
                        }
                    )
                }

                //Genres
                item {
                    val genres: List<String> = viewState.movie.genres.split("|").toList()
                    TextLazyRow(
                        categoryList = genres,
                        onClick = { navController.navigate("movies/category/$it") },
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
                        TextLazyRow(
                            categoryList = stars.toList(),
                            onClick = {},
                            modifier = Modifier.padding(10.dp)
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
                            modifier = Modifier.padding(14.dp)
                        )
                        val directors: Set<String> = viewState.movie.directors
                            .split("|").toSet()
                        TextLazyRow(
                            categoryList = directors.toList(),
                            onClick = {},
                            modifier = Modifier.padding(10.dp)
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
                            modifier = Modifier.padding(14.dp)
                        )
                        val writers: Set<String> = viewState.movie.writers
                            .split("|").toSet()
                        TextLazyRow(
                            categoryList = writers.toList(),
                            onClick = {},
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                item {
                    val recommendedMoviesScrollState = rememberLazyListState()
                    MoviesRowSection(
                        header = "More Like This",
                        lazyListState = recommendedMoviesScrollState,
                        movieList = viewState.recommendedMovies,
                        navController = navController,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
        is MovieDetailsViewState.Error -> {
            Text(text = viewState.msg)
        }
    }
}

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Login icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}