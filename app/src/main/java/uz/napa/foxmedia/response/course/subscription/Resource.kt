package uz.napa.foxmedia.response.course.subscription


import com.google.gson.annotations.SerializedName

data class Resource(
    val id: Int,
    val plan: String,
    val period: Any,
    val price: Int,
    val saleprice: Int,
    val discount: Int?
)