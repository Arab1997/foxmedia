package uz.napa.foxmedia.request.user

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)