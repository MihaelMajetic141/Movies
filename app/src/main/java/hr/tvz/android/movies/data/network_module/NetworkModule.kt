package hr.tvz.android.movies.data.network_module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hr.tvz.android.movies.data.repository.AuthRepository
import hr.tvz.android.movies.data.repository.MovieRepository
import hr.tvz.android.movies.data.repository.RecommendationRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(2, TimeUnit.MINUTES)
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthService(): AuthRepository {
        return RetrofitInstance.getAuthRepository
    }

    @Provides
    @Singleton
    fun provideMovieService(): MovieRepository {
        return RetrofitInstance.getMovieRepository
    }

    @Provides
    @Singleton
    fun provideRecommendationService(): RecommendationRepository {
        return RetrofitInstance.getRecommendationRepository
    }
}