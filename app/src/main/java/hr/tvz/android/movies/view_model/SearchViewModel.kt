package hr.tvz.android.movies.view_model

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.tvz.android.movies.data.repository.MovieRepository
import hr.tvz.android.movies.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchViewState {
    data object Idle : SearchViewState
    data object Loading : SearchViewState
    data class Error(val msg: String) : SearchViewState
    data class SearchResultsFetched(
        val searchResults: List<Movie>
    ) : SearchViewState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository
): ViewModel() {

    private val _searchViewState = MutableStateFlow<SearchViewState>(SearchViewState.Idle)
    val searchViewState: StateFlow<SearchViewState> = _searchViewState

    private val _searchedMoviesPage: MutableStateFlow<Int> = MutableStateFlow(0)
    var searchedMoviesPage: StateFlow<Int> = _searchedMoviesPage.asStateFlow()

    fun search(searchString: String) = viewModelScope.launch {
        val response = movieRepository.searchMovies(searchString, 0)
        if (response != null) _searchViewState.update {
            return@update SearchViewState.SearchResultsFetched(
                searchResults = response.content
            )
        } else _searchViewState.update { SearchViewState.Error("Error fetching movies") }
    }

    fun fetchNextSearchPage(searchString: String) = viewModelScope.launch {
        try {
            _searchedMoviesPage.value += 1
            val response = movieRepository.searchMovies(searchString, _searchedMoviesPage.value)
            if (response != null) {
                _searchViewState.update {
                    val currentMovies = (it as? SearchViewState.SearchResultsFetched)?.searchResults
                        ?: emptyList()
                    return@update SearchViewState.SearchResultsFetched(
                        searchResults = currentMovies + response.content
                    )
                }
            }
        } catch (e: Exception) {
            _searchViewState.update { SearchViewState.Error("") }
        }
    }

}

