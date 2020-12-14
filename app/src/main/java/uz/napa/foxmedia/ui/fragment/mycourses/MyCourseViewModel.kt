package uz.napa.foxmedia.ui.fragment.mycourses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.course.CourseInfoResponse
import uz.napa.foxmedia.response.user.completed_course.CompletedCourseResponse
import uz.napa.foxmedia.response.user.subscription.UserSubscriptionResponse
import uz.napa.foxmedia.ui.fragment.course.ONE_TIME
import uz.napa.foxmedia.util.Event
import uz.napa.foxmedia.util.Resource

class MyCourseViewModel(private val repository: MyRepository) : ViewModel() {

    private val _userCourses = MutableLiveData<Resource<UserSubscriptionResponse>>()
    val userCourses: LiveData<Resource<UserSubscriptionResponse>> = _userCourses

    private val _completedCourses = MutableLiveData<Resource<CompletedCourseResponse>>()
    val completedCourseResponse: LiveData<Resource<CompletedCourseResponse>> = _completedCourses

    private val _courseInfo = MutableLiveData<Event<Resource<CourseInfoResponse>>>()
    val courseInfo: LiveData<Event<Resource<CourseInfoResponse>>> = _courseInfo

    init {
        getUserCourses(ONE_TIME)
        getCompletedCourses()
    }

    fun getUserCourses(subscriptionType: String) = viewModelScope.launch {
        repository.getUserSubscriptions(subscriptionType)
            .onStart { _userCourses.postValue(Resource.Loading()) }
            .catch { _userCourses.postValue(Resource.Error(it.message.toString())) }
            .collect { _userCourses.postValue(Resource.Success(it)) }
    }

    fun getCompletedCourses() = viewModelScope.launch {
        repository.getCompletedCourses()
            .onStart { _completedCourses.postValue(Resource.Loading()) }
            .catch { _completedCourses.postValue(Resource.Error(it.message.toString())) }
            .collect { _completedCourses.postValue(Resource.Success(it)) }
    }

    fun getCourseInfo(courseId: Long) = viewModelScope.launch {
        repository.getCourseInfo(courseId)
            .onStart { _courseInfo.postValue(Event(Resource.Loading())) }
            .catch { _courseInfo.postValue(Event(Resource.Error(it.message.toString()))) }
            .collect { _courseInfo.postValue(Event(Resource.Success(it))) }
    }
}