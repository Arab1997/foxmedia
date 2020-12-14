package uz.napa.foxmedia.request.register

import com.google.gson.annotations.SerializedName

class ResetPasswordRequest (
    @SerializedName("user_id")
    val userId:Long,
    @SerializedName("temp_password")
    val tempPassword:String,
    @SerializedName("password")
    val password:String,
    @SerializedName("password_confirmation")
    val passwordConfirmation:String
)