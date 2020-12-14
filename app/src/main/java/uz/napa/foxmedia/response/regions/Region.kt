package uz.napa.foxmedia.response.regions
import com.google.gson.annotations.SerializedName

data class Region(
    val id: Long,
    @SerializedName("province_id")
    val provinceId: String,
    val num: String,
    val name: String
)