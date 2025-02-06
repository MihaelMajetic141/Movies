package hr.tvz.android.movies.data.repository

import hr.tvz.android.movies.data.payload.LoginRequest
import hr.tvz.android.movies.data.payload.RegistrationRequest
import hr.tvz.android.movies.data.payload.LoginResponse
import hr.tvz.android.movies.view_model.GoogleAuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthRepository {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest?): LoginResponse

    @POST("api/auth/register")
    suspend fun signup(@Body registrationRequest: RegistrationRequest): Response<String>

    @POST("api/auth/logout")
    suspend fun logout(@Body refreshToken: String): Response<String>

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body googleAuthRequest: GoogleAuthRequest): LoginResponse

}