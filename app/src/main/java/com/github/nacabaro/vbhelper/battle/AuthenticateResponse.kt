package com.github.nacabaro.vbhelper.battle

data class AdditionalInfo(
    val avatar: String? = null,
    val id: Long? = null,
    val name: String? = null,
    val status: String? = null
)

data class UserInfo(
    val userId: String? = null,
    val additionalInfo: AdditionalInfo? = null
)

data class AuthenticateResponse(
    val success: Boolean,
    val message: String? = null,
    val userInfo: UserInfo? = null,
    val sessionToken: String? = null
)

