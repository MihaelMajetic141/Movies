package hr.tvz.android.movies.view_model

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


sealed interface HomeScreenViewState {
    data object Loading: HomeScreenViewState
    data class Error(val msg: String): HomeScreenViewState
    data class Success(
        val topRatedMoviesList: List<Movie> = emptyList(),
        val newMoviesList: List<Movie> = emptyList(),
    ) : HomeScreenViewState
}

sealed interface RecommendationsViewState {
    data object Empty: RecommendationsViewState
    data object Loading: RecommendationsViewState
    data class Error(val msg: String): RecommendationsViewState
    data class Success(val recommendedMoviesList: List<Movie>): RecommendationsViewState
}


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private val _homeScreenViewState = MutableStateFlow<HomeScreenViewState>(HomeScreenViewState.Loading)
    var homeScreenViewState: StateFlow<HomeScreenViewState> = _homeScreenViewState.asStateFlow()

    private val _recommendationsViewState = MutableStateFlow<RecommendationsViewState>(
        RecommendationsViewState.Empty
    )
    val recommendationsViewState: StateFlow<RecommendationsViewState> =
        _recommendationsViewState.asStateFlow()

    private val _topRatedMoviesPage: MutableStateFlow<Int> = MutableStateFlow(0)
    var topRatedMoviesPage: StateFlow<Int> = _topRatedMoviesPage
    private val _newMoviesPage: MutableStateFlow<Int> = MutableStateFlow(0)
    var newMoviesPage: StateFlow<Int> = _newMoviesPage
    private val _discoverMoviesPage: MutableStateFlow<Int> = MutableStateFlow(0)
    var discoverMoviesPage: StateFlow<Int> = _discoverMoviesPage
    private val _recommendedMoviesPage: MutableStateFlow<Int> = MutableStateFlow(0)
    var recommendedMoviesPage: StateFlow<Int> = _recommendedMoviesPage

    val allCategoriesList = listOf(
        "Drama",
        "History",
        "Romance",
        "Horror",
        "Mystery",
        "Thriller",
        "Comedy",
        "Family",
        "Fantasy",
        "Documentary",
        "Action",
        "Adventure",
        "Western",
        "Crime",
        "Music",
        "Musical",
        "War", "Biography", "Sci-Fi", "Film-Noir", "Animation", "Sport", "News"
    )

    suspend fun fetchInitialData() {
        viewModelScope.launch {
            _topRatedMoviesPage.value = 0
            _newMoviesPage.value = 0
            val topRatedMoviesResponse = movieRepository.fetchTopRatedMovies(0)
            val newMoviesResponse = movieRepository.fetchAllMovies(0)
            _homeScreenViewState.update {
                return@update HomeScreenViewState.Success(
                    topRatedMoviesList = topRatedMoviesResponse.content,
                    newMoviesList = newMoviesResponse.content,
                )
            }
        }
    }

    suspend fun fetchRecommendedMovies(username: String, accessToken: String) = viewModelScope.launch {
        try {
            _recommendationsViewState.update { RecommendationsViewState.Loading }
            if (username == "") {
                _recommendationsViewState.update { return@update RecommendationsViewState.Empty }
            }
            _recommendedMoviesPage.value = 0
            val currentFavorites = movieRepository.getLikedMovies(username, "Bearer $accessToken") ?: emptyList()
            if (currentFavorites.isEmpty()) {
                _recommendationsViewState.update { return@update RecommendationsViewState.Empty }
            }
            val movie = currentFavorites[_recommendedMoviesPage.value!!]
            val recommendationsResponse = recommendationRepository
                .fetchRecommendations(movie.id)
            _recommendationsViewState.update {
                return@update RecommendationsViewState.Success(recommendationsResponse)
            }
        } catch (e: Exception) {
            RecommendationsViewState.Error(e.message.toString())
        }
    }

    suspend fun fetchMoreRecommendedMovies(username: String, accessToken: String) = viewModelScope.launch {
        try {
            _recommendedMoviesPage.value += 1
            val currentRecommendations = (recommendationsViewState.value as?
                    RecommendationsViewState.Success)?.recommendedMoviesList ?: emptyList()
            val currentFavorites = movieRepository.getLikedMovies(username, "Bearer $accessToken") ?: emptyList()
            val movie = currentFavorites.getOrNull(_recommendedMoviesPage.value)
            if (movie != null) {
                val recommendationsResponse = recommendationRepository
                    .fetchRecommendations(movie.id)
                _recommendationsViewState.update {
                    return@update RecommendationsViewState.Success(
                        recommendedMoviesList = currentRecommendations + recommendationsResponse
                    )
                }
            }
        } catch (e: Exception) {
            RecommendationsViewState.Error(e.message.toString())
        }
    }

    suspend fun fetchNextTopRatedPage() {
        _topRatedMoviesPage.value += 1
        viewModelScope.launch {
            val response = movieRepository.fetchTopRatedMovies(_topRatedMoviesPage.value)
            _homeScreenViewState.update {
                val currentTopRatedMovies = (it as? HomeScreenViewState.Success)
                    ?.topRatedMoviesList?: emptyList()
                val currentNewMovies = (it as? HomeScreenViewState.Success)
                    ?.newMoviesList?: emptyList()

                return@update HomeScreenViewState.Success(
                    topRatedMoviesList = currentTopRatedMovies + response.content,
                    newMoviesList = currentNewMovies,
                )
            }
        }
    }

    suspend fun fetchNextNewMoviesPage() {
        _newMoviesPage.value += 1
        viewModelScope.launch {
            val response = movieRepository.fetchAllMovies(_newMoviesPage.value)
            _homeScreenViewState.update {
                val currentTopRatedMovies = (it as? HomeScreenViewState.Success)
                    ?.topRatedMoviesList?: emptyList()
                val currentNewMovies = (it as? HomeScreenViewState.Success)
                    ?.newMoviesList?: emptyList()

                return@update HomeScreenViewState.Success(
                    topRatedMoviesList = currentTopRatedMovies,
                    newMoviesList = currentNewMovies + response.content,
                )
            }
        }
    }
}