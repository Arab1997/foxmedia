package uz.napa.foxmedia.request.register

import com.google.gson.annotations.SerializedName

class RegisterRequest (
    @SerializedName("first_name")
    val name:String,
    @SerializedName("last_name")
    val surname:String,
    @SerializedName("phone_number")
    val phoneNum:String,
    val password:String,
    @SerializedName("password_confirmation")
    val passwordConfirmation:String,
    @SerializedName("province_id")
    val provinceId:Long,
    @SerializedName("region_id")
    val regionId:Long
)