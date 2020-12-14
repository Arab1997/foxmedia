package uz.napa.foxmedia.response.user
import com.google.gson.annotations.SerializedName

data class ChangePasswordResponse(
    val status: String,
    val message: String
)