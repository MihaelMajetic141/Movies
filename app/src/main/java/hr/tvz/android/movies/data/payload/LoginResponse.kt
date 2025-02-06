package hr.tvz.android.movies.data.payload

import hr.tvz.android.movies.data.model.UserInfoDTO

data class LoginResponse(
    val userInfo: UserInfoDTO,
    val jwtResponse: JwtResponse
)