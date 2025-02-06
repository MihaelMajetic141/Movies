package hr.tvz.android.movies.views.elements

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingState(modifier: Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 128.dp),
        color = Color.Gray
    )
}