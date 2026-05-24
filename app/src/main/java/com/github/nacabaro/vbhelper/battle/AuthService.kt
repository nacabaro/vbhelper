package com.github.nacabaro.vbhelper.battle

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/validate")
    fun validate(@Body request: AuthenticateRequest): Call<AuthenticateResponse>
    
    @POST("api/auth/login")
    fun login(@Body request: AuthenticateRequest): Call<AuthenticateResponse>
}

