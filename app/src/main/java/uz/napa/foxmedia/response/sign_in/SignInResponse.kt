package uz.napa.foxmedia.response.sign_in


import com.google.gson.annotations.SerializedName

data class SignInResponse(
    val status: String,
    val user: User,
    @SerializedName("user_permissions")
    val userPermissions: List<UserPermission>,
    val token: String,
    val session: Session
)