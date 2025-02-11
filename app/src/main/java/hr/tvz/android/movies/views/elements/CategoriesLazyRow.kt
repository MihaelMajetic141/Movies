package hr.tvz.android.movies.views.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.tvz.android.movies.ui.theme.primaryContainerDark

@Composable
fun CategoriesLazyRow(
    categoryList: List<String>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Genres",
            fontSize = 24.sp,
            color = Color.White,
            modifier = modifier
                .padding(top = 10.dp)
        )
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            content = {
                categoryList.forEach {
                    item {
                        Text(
                            text = it,
                            color = Color.White,
                            modifier = modifier
                                .background(
                                    color = primaryContainerDark,
                                    shape = CircleShape
                                )
                                .padding(horizontal = 14.dp)
                                .clickable { onCategoryClick(it) }
                        )
                    }
                }
            }
        )
    }
}