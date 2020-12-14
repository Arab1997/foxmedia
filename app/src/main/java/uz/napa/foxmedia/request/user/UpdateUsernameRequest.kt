package uz.napa.foxmedia.request.user

import com.google.gson.annotations.SerializedName

data class UpdateUsernameRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)