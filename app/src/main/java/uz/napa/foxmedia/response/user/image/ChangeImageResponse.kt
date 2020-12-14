package uz.napa.foxmedia.response.user.image


import com.google.gson.annotations.SerializedName

data class ChangeImageResponse(
    val status: String,
    val image: String,
    val thumbnail: String
)