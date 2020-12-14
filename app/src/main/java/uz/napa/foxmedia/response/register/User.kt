package uz.napa.foxmedia.response.register


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("role_id")
    val roleId: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("created_at")
    val createdAt: String,
    val id: Int
)