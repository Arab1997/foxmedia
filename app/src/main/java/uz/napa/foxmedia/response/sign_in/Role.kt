package uz.napa.foxmedia.response.sign_in


import com.google.gson.annotations.SerializedName

data class Role(
    val id: Int,
    val name: String,
    val label: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)