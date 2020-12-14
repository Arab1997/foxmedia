package uz.napa.foxmedia.response.user.session


import com.google.gson.annotations.SerializedName

data class SessionsResponse(
    val status: String,
    @SerializedName("user_sessions")
    val userSessions: List<UserSession>
)