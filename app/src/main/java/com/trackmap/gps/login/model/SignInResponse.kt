package com.trackmap.gps.login.model

import com.google.gson.annotations.SerializedName

data class SignInResponse(

    @field:SerializedName("access_token")
    val accessToken: String? = null,

    @field:SerializedName("refresh_token")
    val refreshToken: String? = null,

    @field:SerializedName("user_detail")
    val userDetail: UserDetail? = null,

    @field:SerializedName("token_type")
    val tokenType: String? = null,

    @field:SerializedName("expires_in")
    val expiresIn: Int? = null
) {

    data class UserDetail(

        @field:SerializedName("id")
        val id: String? = null,

        @field:SerializedName("owner_id")
        val ownerId: String? = null,

        @field:SerializedName("is_bind_allow")
        val isBindAllow: Int? = null
    )
}