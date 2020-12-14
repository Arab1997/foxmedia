package uz.napa.foxmedia.response.category


import com.google.gson.annotations.SerializedName

data class SubcategoryResponse(
    val status: String,
    val category: Category
)