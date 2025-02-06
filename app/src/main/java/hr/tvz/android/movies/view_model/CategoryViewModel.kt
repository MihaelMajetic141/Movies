package hr.tvz.android.movies.view_model

import hr.tvz.android.movies.data.repository.MovieRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.tvz.android.movies.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val movieRepository: MovieRepository
): ViewModel() {

    sealed interface CategoryViewState {
        data object Loading : CategoryViewState
        data class Error(val msg: String) : CategoryViewState
        data class Success(val movieList: List<Movie>) : CategoryViewState
    }

    private val _categoryViewState = MutableStateFlow<CategoryViewState>(CategoryViewState.Loading)
    var categoryViewState: StateFlow<CategoryViewState> = _categoryViewState.asStateFlow()

    private val _page = MutableStateFlow(0)
    var page: StateFlow<Int> = _page.asStateFlow()

    fun fetchMoviesByGenre(categoryName: String) {
        viewModelScope.launch {
            val response = movieRepository.fetchMoviesByGenre(categoryName, 0)
            if (response != null) {
                _categoryViewState.update {
                    return@update CategoryViewState.Success(movieList = response.content)
                }
            } else {
                _categoryViewState.update {
                    return@update CategoryViewState.Error("Error fetching movies")
                }
            }
        }
    }

    fun fetchNextGenrePage(genre: String) {
        _page.value += 1
        viewModelScope.launch {
            try {
                val response = movieRepository.fetchMoviesByGenre(genre, _page.value)
                if (response != null) {
                    _categoryViewState.update {
                        val currentMovies = (it as? CategoryViewState.Success)?.movieList
                            ?: emptyList()
                        return@update CategoryViewState.Success(
                            movieList = currentMovies + response.content
                        )
                    }
                }
            } catch (e: Exception) {
                _categoryViewState.update {
                    return@update CategoryViewState.Error(e.message.toString())
                }
            }
        }
    }
}