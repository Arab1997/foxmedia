package uz.napa.foxmedia.response.user.session


import com.google.gson.annotations.SerializedName

data class UserSession(
    val id: String,
    @SerializedName("user_id")
    val userId: Long,
    val ip: String,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("create_time")
    val createTime: String,
    @SerializedName("signed_in_time")
    val signedInTime: String,
    @SerializedName("logged_out_time")
    val loggedOutTime: String?
)