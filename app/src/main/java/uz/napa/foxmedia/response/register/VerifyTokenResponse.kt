package uz.napa.foxmedia.response.register


import com.google.gson.annotations.SerializedName

data class VerifyTokenResponse(
    val status: String,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("temp_password")
    val tempPassword: String
)