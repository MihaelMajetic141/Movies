package hr.tvz.android.movies.data.network_module

import hr.tvz.android.movies.data.repository.AuthRepository
import hr.tvz.android.movies.data.repository.MovieRepository
import hr.tvz.android.movies.data.repository.RecommendationRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object RetrofitInstance {
    private val logging: HttpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val moviesRetrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()
            )
            .build()
    }

    private val recommendationRetrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()
            )
            .build()
    }

    val getAuthRepository: AuthRepository by lazy {
        moviesRetrofitInstance.create(AuthRepository::class.java)
    }

    val getMovieRepository: MovieRepository by lazy {
        moviesRetrofitInstance.create(MovieRepository::class.java)
    }

    val getRecommendationRepository: RecommendationRepository by lazy {
        recommendationRetrofitInstance.create(RecommendationRepository::class.java)
    }
}