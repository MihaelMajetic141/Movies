package hr.tvz.android.movies.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.tvz.android.movies.data.repository.MovieRepository
import hr.tvz.android.movies.data.repository.RecommendationRepository
import hr.tvz.android.movies.views.MovieDetailsViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<MovieDetailsViewState>(MovieDetailsViewState.Loading)
    var viewState: StateFlow<MovieDetailsViewState> = _viewState.asStateFlow()

    private val _isMovieInWatchList = MutableStateFlow(false)
    var isMovieInWatchList: StateFlow<Boolean> = _isMovieInWatchList.asStateFlow()

    private val _isMovieInFavorites = MutableStateFlow(false)
    var isMovieInFavorites: StateFlow<Boolean> = _isMovieInFavorites.asStateFlow()

    fun fetchInitialData(id: Long) = viewModelScope.launch {
        try {
            val movieResponse = movieRepository.fetchMovieById(id)
            val recommendationsResponse = recommendationRepository.fetchRecommendations(id)
            if (movieResponse != null) {
                _viewState.update {
                    return@update MovieDetailsViewState.Success(
                        movie = movieResponse,
                        recommendedMovies = recommendationsResponse
                    )
                }
            }
        } catch (e: Exception) {
            MovieDetailsViewState.Error(e.message.toString())
        }
    }

    fun checkIfMovieIsInWatchList(movieId: Long, username: String, accessToken: String) = viewModelScope.launch {
        val movie = movieRepository.fetchMovieById(movieId)
        val watchList = movieRepository.getWatchLaterMovies(username, "Bearer $accessToken")
        if (movie != null && watchList != null) {
            val isMovieInWatchList = watchList.any { it.id == movie.id }
            return@launch _isMovieInWatchList.update { isMovieInWatchList }
        } else return@launch _isMovieInWatchList.update { false }
    }

    fun checkIfMovieIsInFavorites(movieId: Long, username: String, accessToken: String) = viewModelScope.launch {
        val movie = movieRepository.fetchMovieById(movieId)
        val favorites = movieRepository.getLikedMovies(username, "Bearer $accessToken")
        if (movie != null && favorites != null) {
            val isMovieInFavorites = favorites.any { it.id == movie.id }
            return@launch _isMovieInFavorites.update { isMovieInFavorites }
        } else return@launch _isMovieInFavorites.update { false }
    }
}

