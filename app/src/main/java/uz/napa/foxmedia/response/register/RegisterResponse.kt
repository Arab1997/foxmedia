package uz.napa.foxmedia.response.register


import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    val status: String,
    val user: User,
    val message: String
)