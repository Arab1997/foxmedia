package uz.napa.foxmedia.response.regions


data class BaseProvincesResponse<T>(
    val status: String,
    val provinces: List<T>,
    val regions: List<T>
)