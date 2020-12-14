package uz.napa.foxmedia.response.search


import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val status: String,
    val results: Results
)