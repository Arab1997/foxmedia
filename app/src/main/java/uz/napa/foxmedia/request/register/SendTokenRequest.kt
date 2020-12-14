package uz.napa.foxmedia.request.register

import com.google.gson.annotations.SerializedName

class SendTokenRequest(
    @SerializedName("phone_number")
    val phoneNum:Long
)