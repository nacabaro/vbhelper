package com.github.nacabaro.vbhelper.battle

import com.google.gson.annotations.SerializedName

data class AdditionalInfo(
    @SerializedName(value = "avatar", alternate = ["avatar_url"])
    val avatar: String? = null,
    @SerializedName(value = "id", alternate = ["user_id"])
    val id: Long? = null,
    @SerializedName(value = "name", alternate = ["username", "display_name"])
    val name: String? = null,
    val status: String? = null
)

data class UserInfo(
    @SerializedName(value = "userId", alternate = ["user_id", "id"])
    val userId: String? = null,
    @SerializedName(value = "additionalInfo", alternate = ["additional_info"])
    val additionalInfo: AdditionalInfo? = null
)

data class AuthenticateResponse(
    @SerializedName(value = "success", alternate = ["ok"])
    val success: Boolean,
    val message: String? = null,
    @SerializedName(value = "userInfo", alternate = ["user_info", "user"])
    val userInfo: UserInfo? = null,
    @SerializedName(value = "sessionToken", alternate = ["session_token", "token", "session"])
    val sessionToken: String? = null,
    @SerializedName(value = "userId", alternate = ["user_id"])
    val topLevelUserId: String? = null,
    @SerializedName(value = "id", alternate = ["uid"])
    val topLevelId: Long? = null,
)

fun AuthenticateResponse.extractSessionToken(): String? {
    return sessionToken?.takeIf { it.isNotBlank() }
}

fun AuthenticateResponse.extractUserId(): Long? {
    return userInfo?.userId?.toLongOrNull()
        ?: topLevelUserId?.toLongOrNull()
        ?: userInfo?.additionalInfo?.id
        ?: topLevelId
}

