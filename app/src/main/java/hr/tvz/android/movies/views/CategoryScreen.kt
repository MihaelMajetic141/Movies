package hr.tvz.android.movies.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import hr.tvz.android.movies.TopAppBarState
import hr.tvz.android.movies.views.elements.LoadingState
import hr.tvz.android.movies.views.elements.MovieVerticalGrid
import hr.tvz.android.movies.view_model.CategoryViewModel
import hr.tvz.android.movies.views.elements.TopBarInit

@Composable
fun CategoryScreen(
    categoryName: String,
    topAppBarState: MutableState<TopAppBarState>,
    navController: NavController,
    onMovieClick: (Long) -> Unit,
    onLongClick: (Long) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val viewModelState by viewModel.categoryViewState.collectAsState()

    TopBarInit(
        topAppBarState = topAppBarState,
        navController = navController,
        title = categoryName,
    )

    LaunchedEffect(key1 = viewModel, block = { viewModel.fetchMoviesByGenre(categoryName) })

    val scrollState = rememberLazyGridState()
    val fetchNextPage: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0
                    && lastVisibleItem?.index == scrollState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(key1 = fetchNextPage) {
        if (fetchNextPage) {
            viewModel.fetchNextGenrePage(categoryName)
        }
    }

    when(val viewState = viewModelState) {
        CategoryViewModel.CategoryViewState.Loading -> LoadingState(modifier = Modifier)

        is CategoryViewModel.CategoryViewState.Success -> {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(60.dp))
                MovieVerticalGrid(
                    navController = navController,
                    scrollState = scrollState,
                    viewState = viewState,
                )
            }
        }

        is CategoryViewModel.CategoryViewState.Error -> {
            Text(text = viewState.msg)
        }
    }
}

