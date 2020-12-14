package uz.napa.foxmedia.request.register

import com.google.gson.annotations.SerializedName

class SignInRequest(
    val username:String,
    val password:String,
    @SerializedName("session_id")
    val sessionId:String?,
    val device:String,
    @SerializedName("device_type")
    val deviceType:String,
    val os:String,
    val os_version:String
)