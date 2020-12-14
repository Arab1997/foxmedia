package uz.napa.foxmedia.request.analytics

import com.google.gson.annotations.SerializedName

data class WatchTimeRequest(
    @SerializedName("video_id")
    val videoId:Long,
    val position:Double,
    val duration:Double,
    val viewable:Int
)