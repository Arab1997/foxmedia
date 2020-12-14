package uz.napa.foxmedia.response.category


import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    val status: String,
    val categories: List<Category>
)