package uz.napa.foxmedia.response.register


import com.google.gson.annotations.SerializedName

data class ConfirmResponse(
    val status: String,
    val message: String
)