package uz.napa.foxmedia.response.sign_in


import com.google.gson.annotations.SerializedName

data class UserPermission(
    val id: Int,
    val name: String,
    val label: String,
    @SerializedName("model_name")
    val modelName: String
)