package uz.napa.foxmedia.request.comment

import uz.napa.foxmedia.util.Constants.Companion.PATCH

data class EditCommentRequest(
    val body: String,
    val _method: String = PATCH
)