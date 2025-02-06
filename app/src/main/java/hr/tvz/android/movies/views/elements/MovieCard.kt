package hr.tvz.android.movies.views.elements

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.R
import hr.tvz.android.movies.data.model.Movie

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieCard(
    modifier: Modifier,
    movie: Movie,
    onLongClick: (Long) -> Unit,
    onClick: (Long) -> Unit
) {
    Card(modifier.height(281.dp).width(190.dp)
        .padding(8.dp)
        .clip(CardDefaults.elevatedShape)
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Gray)),
            shape = CardDefaults.elevatedShape)
        .combinedClickable(
            onLongClick = { onLongClick(movie.id) },
            onClick = { onClick(movie.id) }
        )
    ) {
        SubcomposeAsyncImage(
            model = movie.posterUrl,
            contentDescription = "Movie poster image",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun MockMovieCard(
    modifier: Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Movie poster image",
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Gray)),
                    shape = CardDefaults.elevatedShape
                )
                .clip(CardDefaults.shape)
        )
    }
}

@Preview
@Composable
fun MockMovieCardPreview() {
    MockMovieCard(
        modifier = Modifier,
    )
}