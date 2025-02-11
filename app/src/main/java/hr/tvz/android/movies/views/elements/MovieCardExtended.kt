package hr.tvz.android.movies.views.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import hr.tvz.android.movies.R
import hr.tvz.android.movies.data.model.Movie
import hr.tvz.android.movies.ui.theme.primaryLightHighContrast

@Composable
fun MovieCardExtended(
    modifier: Modifier,
    movie: Movie,
    navController: NavController
) {
    Card(
        shape = CardDefaults.elevatedShape,
        colors = CardDefaults.cardColors(
            primaryLightHighContrast,
        ),
        modifier = modifier
            .width(190.dp)
            .height(360.dp)
            .clickable { navController.navigate("movies/details/${movie.id}") }
    ) {
        SubcomposeAsyncImage(
            model = movie.posterUrl,
            contentDescription = "Movie poster image",
            modifier = Modifier
                .fillMaxWidth()
                .height(281.dp)
                .padding(5.dp)
        )
        Column(modifier = modifier.padding(5.dp)) {
            Text(
                text = movie.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis, //ToDo: Check if this is needed
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = modifier.basicMarquee(
                        iterations = 4,
                        repeatDelayMillis = 1500
                    )
            )
            Row(modifier = modifier.padding(5.dp)) {
                Icon(
                    imageVector = Icons.Default.Star,
                    tint = Color.Yellow,
                    contentDescription = "Star icon",
                )
                Text(
                    text = movie.imdbRating.toString(),
                    color = Color.LightGray,
                    modifier = modifier.padding(horizontal = 5.dp)
                )
            }
        }
    }
}