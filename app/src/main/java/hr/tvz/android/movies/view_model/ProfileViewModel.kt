package hr.tvz.android.movies.view_model

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.tvz.android.movies.data.repository.MovieRepository
import hr.tvz.android.movies.data.repository.RecommendationRepository
import hr.tvz.android.movies.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface ProfileViewState {
    data object Loading: ProfileViewState
    data class Error(val msg: String): ProfileViewState
    data class Success(
        val watchList: List<Movie> = emptyList(),
        val likedList: List<Movie> = emptyList(),
    ) : ProfileViewState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private val _profileViewState = MutableStateFlow<ProfileViewState>(ProfileViewState.Loading)
    var profileViewState: StateFlow<ProfileViewState> = _profileViewState.asStateFlow()

    private val _recommendationsViewState = MutableStateFlow<RecommendationsViewState>(
        RecommendationsViewState.Loading
    )
    val recommendationsViewState: StateFlow<RecommendationsViewState> =
        _recommendationsViewState.asStateFlow()

    private val _movieForRecommendationId: MutableStateFlow<Long> = MutableStateFlow(0)
    var movieForRecommendationId: StateFlow<Long> = _movieForRecommendationId

    suspend fun fetchInitialData(username: String, accessToken: String) = viewModelScope.launch {
        try {
            val watchList = movieRepository.getWatchLaterMovies(username, "Bearer $accessToken")
            val likedList = movieRepository.getLikedMovies(username, "Bearer $accessToken")

            _profileViewState.update {
                return@update ProfileViewState.Success(
                    watchList = watchList?: emptyList(),
                    likedList = likedList?: emptyList(),
                )
            }
        } catch (e: Exception) {
            _profileViewState.update { ProfileViewState.Error(e.message ?: "An error occurred") }
        }
    }

    suspend fun fetchInitialRecommendations() = viewModelScope.launch {
        try {
            _movieForRecommendationId.value = 0
            val currentFavorites = (profileViewState.value as? ProfileViewState.Success)
                ?.likedList?: emptyList()
            val recommendedList = recommendationRepository
                .fetchRecommendations(currentFavorites.first().id)
            if (currentFavorites.isNotEmpty()) {
                _recommendationsViewState.update {
                    return@update RecommendationsViewState.Success(
                        recommendedMoviesList = recommendedList
                    )
                }
            } else
                _recommendationsViewState.update {
                    RecommendationsViewState.Error("No favorites found")
                }
        } catch (e: Exception) {
            _recommendationsViewState.update { RecommendationsViewState.Error(e.message
                ?: "An error occurred") }
        }
    }
    suspend fun fetchMoreRecommendations() = viewModelScope.launch {
        try {
            _movieForRecommendationId.value += 1
            val currentRecommendations = (recommendationsViewState.value as?
                    RecommendationsViewState.Success)?.recommendedMoviesList ?: emptyList()
            val currentFavorites = (profileViewState.value as? ProfileViewState.Success)?.likedList
                ?: emptyList()
            val movie = currentFavorites.getOrNull(_movieForRecommendationId.value.toInt())
            if (movie != null) {
                val recommendedList = recommendationRepository.fetchRecommendations(movie.id)
                _recommendationsViewState.update {
                    return@update RecommendationsViewState.Success(
                        recommendedMoviesList = currentRecommendations + recommendedList
                    )
                }
            }
        } catch (e: Exception) {
            _recommendationsViewState.update { RecommendationsViewState.Error(e.message
                ?: "An error occurred") }
        }
    }

    fun addMovieToWatchList(context: Context, movieId: Long, username: String, accessToken: String) {
        viewModelScope.launch {
            try {
                val newMovie = movieRepository.addMovieToWatchLaterList(
                    movieId = movieId, username = username, token = "Bearer $accessToken"
                )
                _profileViewState.update {
                    val currentWatchlist = (it as? ProfileViewState.Success)?.watchList?: emptyList()
                    return@update ProfileViewState.Success(
                        watchList = currentWatchlist + newMovie
                    )
                }
                Toast.makeText(context, "Movie added to Watchlist!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _profileViewState.update {
                    ProfileViewState.Error(e.message ?: "An error occurred")
                }
            }
        }
    }

    fun addMovieToFavorites(
        context: Context,
        movieId: Long,
        username: String,
        accessToken: String
    ) = viewModelScope.launch {
        try {
            val newMovie = movieRepository.addMovieToLikedList(
                movieId = movieId, username = username, token = "Bearer $accessToken"
            )
            _profileViewState.update {
                val currentFavorites = (it as? ProfileViewState.Success)?.likedList?: emptyList()
                return@update ProfileViewState.Success(
                    likedList = currentFavorites + newMovie
                )
            }
            Toast.makeText(context, "Movie added to Favorites!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            _profileViewState.update {
                ProfileViewState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun removeMovieFromWatchList(
        context: Context,
        movieId: Long,
        username: String,
        accessToken: String
    ) = viewModelScope.launch {
        try {
            movieRepository.removeMovieFromWatchLaterList(username, movieId, "Bearer $accessToken")
            _profileViewState.update { it ->
                val currentWatchlist = (it as? ProfileViewState.Success)?.watchList?: emptyList()
                val currentLikedList = (it as? ProfileViewState.Success)?.likedList?: emptyList()
                return@update ProfileViewState.Success(
                    watchList = currentWatchlist.filter { it.id != movieId },
                    likedList = currentLikedList
                )
            }
            Toast.makeText(context, "Movie removed from Watchlist!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            _profileViewState.update {
                ProfileViewState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun removeMovieFromFavorites(
        context: Context,
        movieId: Long,
        username: String,
        accessToken: String
    ) = viewModelScope.launch {
        try {
            movieRepository.removeMovieFromLikedList(username, movieId, "Bearer $accessToken")
            _profileViewState.update { it ->
                val currentFavorites = (it as? ProfileViewState.Success)?.likedList?: emptyList()
                val currentWatchlist = (it as? ProfileViewState.Success)?.watchList?: emptyList()
                return@update ProfileViewState.Success(
                    likedList = currentFavorites.filter { it.id != movieId },
                    watchList = currentWatchlist
                )
            }
            Toast.makeText(context, "Movie removed from Favorites!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            _profileViewState.update {
                ProfileViewState.Error(e.message ?: "An error occurred")
            }
        }
    }

}