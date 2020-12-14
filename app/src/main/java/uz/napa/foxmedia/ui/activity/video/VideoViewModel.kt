package uz.napa.foxmedia.ui.activity.video

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.analytics.WatchTimeRequest
import uz.napa.foxmedia.request.comment.CreateCommentRequest
import uz.napa.foxmedia.request.comment.EditCommentRequest
import uz.napa.foxmedia.response.analytics.WatchHistoryResponse
import uz.napa.foxmedia.response.course.videos.CourseVideoResponse
import uz.napa.foxmedia.response.video.VideoInfoResponse
import uz.napa.foxmedia.response.video.comment.CommentsResponse
import uz.napa.foxmedia.response.video.comment.CreateCommentResponse
import uz.napa.foxmedia.response.video.comment.EditCommentResponse
import uz.napa.foxmedia.response.video.files.VideoFilesResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class VideoViewModel(private val repository: MyRepository, courseId: Long) : ViewModel() {

    private val _courseVideos = MutableLiveData<Resource<CourseVideoResponse>>()
    val courseVideos: LiveData<Resource<CourseVideoResponse>> = _courseVideos

    private val _videoInfo = MutableLiveData<Resource<VideoInfoResponse>>()
    val videoInfo: LiveData<Resource<VideoInfoResponse>> = _videoInfo

    private val _comments = MutableLiveData<Resource<CommentsResponse>>()
    val comments: LiveData<Resource<CommentsResponse>> = _comments

    private val _createdComment = MutableLiveData<Resource<CreateCommentResponse>>()
    val createdComment: LiveData<Resource<CreateCommentResponse>> = _createdComment

    private val _deleteComment = MutableLiveData<Resource<WatchHistoryResponse>>()
    val deleteComment: LiveData<Resource<WatchHistoryResponse>> = _deleteComment

    private val _editComment = MutableLiveData<Resource<EditCommentResponse>>()
    val editComment: LiveData<Resource<EditCommentResponse>> = _editComment

    private val _files = MutableLiveData<Resource<VideoFilesResponse>>()
    val files: LiveData<Resource<VideoFilesResponse>> = _files

    var commentsPage = 1
    private var commentsResponse: CommentsResponse? = null

    init {
        getCourseVideo(courseId)
    }

    fun getComments(videoId: Long) = viewModelScope.launch {
        repository.getVideoComments(videoId, commentsPage)
            .onStart { _comments.postValue(Resource.Loading()) }
            .catch {
                _comments.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                commentsPage++
                if (commentsResponse == null) {
                    commentsResponse = it
                } else {
                    val oldList = commentsResponse!!.comments.data
                    val newList = it.comments.data
                    oldList.addAll(newList)
                }
                _comments.postValue(Resource.Success(commentsResponse ?: it))
            }
    }

    fun getCourseVideo(courseId: Long) = viewModelScope.launch {
        repository.getCourseVideos(courseId)
            .onStart { _courseVideos.postValue(Resource.Loading()) }
            .catch {
                _courseVideos.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                _courseVideos.postValue(Resource.Success(it))
            }
    }

    fun getVideoInfo(videoId: Long) = viewModelScope.launch {
        repository.getVideoInfo(videoId)
            .onStart { _videoInfo.postValue(Resource.Loading()) }
            .catch {
                _videoInfo.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                _videoInfo.postValue(Resource.Success(it))
            }
    }


    fun getFiles(videoId: Long) = viewModelScope.launch {
        repository.getVideoFiles(videoId)
            .onStart { _files.postValue(Resource.Loading()) }
            .catch {
                _files.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                _files.postValue(Resource.Success(it))
            }
    }

    fun addVideoToHistory(videoId: Long) = viewModelScope.launch {
        repository.addVideoToHistory(videoId)
            .onStart { }
            .catch { }
            .collect { }
    }

    fun watchTimeRequest(watchTimeRequest: WatchTimeRequest) = viewModelScope.launch {
        repository.watchTimeAnalytics(watchTimeRequest)
            .onStart { }
            .catch { }
            .collect { }
    }

    fun createComment(createCommentRequest: CreateCommentRequest) = viewModelScope.launch {
        repository.createComment(createCommentRequest)
            .onStart { _createdComment.postValue(Resource.Loading()) }
            .catch {
                _createdComment.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                _createdComment.postValue(Resource.Success(it))
            }
    }

    fun editComment(commentId: Long, editCommentRequest: EditCommentRequest) =
        viewModelScope.launch {
            repository.editComment(commentId, editCommentRequest)
                .onStart { _editComment.postValue(Resource.Loading()) }
                .catch {
                    _editComment.postValue(Resource.Error(handleError(it)))
                }
                .collect {
                    _editComment.postValue(Resource.Success(it))
                }
        }

    fun deleteComment(commentId: Long) = viewModelScope.launch {
        repository.deleteComment(commentId)
            .onStart { _deleteComment.postValue(Resource.Loading()) }
            .catch {
                _deleteComment.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                _deleteComment.postValue(Resource.Success(it))
            }
    }

    private fun handleError(error: Throwable): String {
        val context = App.appInstance
        return when (error) {
            is IOException -> {
                context.getString(R.string.no_connection)
            }
            is HttpException -> {
                when {
                    error.code() == 500 -> context.getString(R.string.server_error)
                    error.code() == 401 -> context.getString(R.string.log_out)
                    else -> context.getString(R.string.connection_error)
                }
            }
            else -> {
                context.getString(R.string.unknown_error)
            }
        }
    }

}