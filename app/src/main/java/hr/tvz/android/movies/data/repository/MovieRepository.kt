package hr.tvz.android.movies.data.repository

import hr.tvz.android.movies.data.model.Movie
import hr.tvz.android.movies.data.payload.PagedListResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface MovieRepository {
    @GET("/movies/browse")
    suspend fun fetchAllMovies(@Query("page") page: Int): PagedListResponse<Movie>

    @GET("/movies")
    suspend fun fetchMovieById(@Query("id") id: Long): Movie?

    @GET("/movies/title")
    suspend fun fetchMovieByTitle(@Query("title") title: String): Movie?

    @GET("/movies/titles")
    suspend fun fetchMoviesByTitles(@Query("titles") titles: List<String>): List<Movie>?

    @GET("/movies/title/{title}")
    suspend fun searchMovies(
        //@Header("Authorization") String token,
        @Path("title") title: String?,
        @Query("page") page: Int?
    ): PagedListResponse<Movie>?

    @GET("/movies/genre/{genre}")
    suspend fun fetchMoviesByGenre(
        //@Header("Authorization") String token,
        @Path("genre") genre: String?,
        @Query("page") page: Int?
    ): PagedListResponse<Movie>?

    @GET("/movies/topRated")
    suspend fun fetchTopRatedMovies(@Query("page") page: Int): PagedListResponse<Movie>


    @GET("/movies/user/getWatchLaterMovies/{username}")
    suspend fun getWatchLaterMovies(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): List<Movie>?

    @GET("/movies/user/getLikedMovies/{username}")
    suspend fun getLikedMovies(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): List<Movie>?

    @POST("/movies/user/addMovieToLikedList")
    suspend fun addMovieToLikedList(
        @Query("username") username: String,
        @Query("movieId") movieId: Long,
        @Header("Authorization") token: String
    ): Movie

    @POST("/movies/user/addMovieToWatchLaterList")
    suspend fun addMovieToWatchLaterList(
        @Query("username") username: String,
        @Query("movieId") movieId: Long,
        @Header("Authorization") token: String
    ): Movie

    @DELETE("/movies/user/removeMovieFromLikedList")
    suspend fun removeMovieFromLikedList(
        @Query("username") username: String,
        @Query("movieId") movieId: Long,
        @Header("Authorization") token: String,
    ): Response<Movie>

    @DELETE("/movies/user/removeMovieFromWatchLaterList")
    suspend fun removeMovieFromWatchLaterList(
        @Query("username") username: String,
        @Query("movieId") movieId: Long,
        @Header("Authorization") token: String
    ): Response<Movie>
}