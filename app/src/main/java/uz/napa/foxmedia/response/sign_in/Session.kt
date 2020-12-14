package uz.napa.foxmedia.response.sign_in


import com.google.gson.annotations.SerializedName

data class Session(
    val id: String,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("signed_in_time")
    val signedInTime: String
)