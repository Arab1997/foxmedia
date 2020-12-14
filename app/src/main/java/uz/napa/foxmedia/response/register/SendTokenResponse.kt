package uz.napa.foxmedia.response.register


import com.google.gson.annotations.SerializedName

data class SendTokenResponse(
    val status: String,
    val message: String
)