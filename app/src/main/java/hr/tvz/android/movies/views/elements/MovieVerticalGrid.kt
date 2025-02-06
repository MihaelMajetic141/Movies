package hr.tvz.android.movies.views.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import hr.tvz.android.movies.ui.theme.onPrimaryDark
import hr.tvz.android.movies.view_model.CategoryViewModel


//TODO: Fix layout
@Composable
fun MovieVerticalGrid(
    navController: NavController,
    scrollState: LazyGridState,
    viewState: CategoryViewModel.CategoryViewState
) {
    LazyVerticalGrid(
        state = scrollState,
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxHeight()
            .background(onPrimaryDark)
            .padding(8.dp),
        content = {
            items(
                items = (viewState as CategoryViewModel.CategoryViewState.Success).movieList,
                key = { it.id }
            ) { movie ->
                MovieCardExtended(
                    movie = movie,
                    navController = navController,
                    modifier = Modifier
                )
            }
        }

    )
}