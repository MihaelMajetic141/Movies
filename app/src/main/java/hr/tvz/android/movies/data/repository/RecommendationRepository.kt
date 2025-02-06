package hr.tvz.android.movies.data.repository

import hr.tvz.android.movies.data.model.Movie
import retrofit2.http.GET
import retrofit2.http.Query

interface RecommendationRepository {
    @GET("/recommendations")
    suspend fun fetchRecommendations(@Query("movie_id") id: Long): List<Movie>
}