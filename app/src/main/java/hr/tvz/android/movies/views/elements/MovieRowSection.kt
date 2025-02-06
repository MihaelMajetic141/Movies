package hr.tvz.android.movies.views.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import hr.tvz.android.movies.data.model.Movie
import hr.tvz.android.movies.ui.theme.primaryLightHighContrast

@Composable
fun MoviesRowSection(
    header: String,
    lazyListState: LazyListState,
    movieList: List<Movie>,
    navController: NavController,
    modifier: Modifier = Modifier.padding(vertical = 10.dp)
) {
    Text(text = header, fontSize = 24.sp, color = Color.White, modifier = modifier.padding(5.dp))
    LazyRow(
        modifier = Modifier.fillMaxWidth().background(primaryLightHighContrast),
        state = lazyListState,
        content = {
            if (movieList.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                            .padding(75.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
                    ) {
                        // ToDo: Add IconButton for log in
                        Text(
                            text = "No movies found",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(5.dp).align(Alignment.Center)
                        )
                    }
                }
            } else {
                items(
                    items = movieList,
                    key = { it.id }
                ) { movie ->
                    MovieCard(
                        movie = movie,
                        onLongClick = {  }, // ToDo: Handle long click
                        onClick = {
                            movieId -> navController.navigate("movies/details/$movieId")
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    )
}