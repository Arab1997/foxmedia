package uz.napa.foxmedia.request.register

import com.google.gson.annotations.SerializedName

class ConfirmRequest (
    @SerializedName("phone_number")
    val phoneNum:Long,
    val token:Int
)