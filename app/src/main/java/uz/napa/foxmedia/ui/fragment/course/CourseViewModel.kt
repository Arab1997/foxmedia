package uz.napa.foxmedia.ui.fragment.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.response.course.CourseInfoResponse
import uz.napa.foxmedia.response.course.subscription.CourseSubscriptionResponse
import uz.napa.foxmedia.response.user.subscription.UserSubscriptionResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class CourseViewModel(private val repository: MyRepository, private val course: Course) :
    ViewModel() {
    private val _courseInfo = MutableLiveData<Resource<CourseInfoResponse>>()
    val courseInfo: LiveData<Resource<CourseInfoResponse>> = _courseInfo

    private val _userSubscription = MutableLiveData<Resource<UserSubscriptionResponse>>()
    val userSubscription: LiveData<Resource<UserSubscriptionResponse>> = _userSubscription

    private val _courseSubscription = MutableLiveData<Resource<CourseSubscriptionResponse>>()
    val courseSubscription: LiveData<Resource<CourseSubscriptionResponse>> =
        _courseSubscription

    private val _savedCourses = MutableLiveData<Resource<List<Course>>>()
    val savedCourses: LiveData<Resource<List<Course>>> = _savedCourses
//
//    init {
//        if (course.subscriptionType == ONE_TIME)
//            getCourseSupscription(course.subscriptionType, course.id)
//        getUserSubscription(course.subscriptionType)
//        getAllSavedCourses()
//        getCourseInfo(course.id)
//    }

    fun getUserSubscription(subscriptionType: String) = viewModelScope.launch {
        repository.getUserSubscriptions(subscriptionType)
            .onStart { _userSubscription.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _userSubscription.postValue((Resource.Error(context.getString(R.string.no_connection))))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _userSubscription.postValue((Resource.Error(context.getString(R.string.server_error))))
                        else
                            _userSubscription.postValue((Resource.Error(context.getString(R.string.connection_error))))
                    }
                    else -> {
                        _userSubscription.postValue((Resource.Error(context.getString(R.string.unknown_error))))
                    }
                }
            }
            .collect {
                _userSubscription.postValue(Resource.Success(it))
            }
    }



    fun getAllSavedCourses() = viewModelScope.launch {
        _savedCourses.postValue(Resource.Loading())
        val courses = repository.getAllWishCourses()
        if (courses == null) {
            _savedCourses.postValue(Resource.Error(""))
        } else
            _savedCourses.postValue(Resource.Success(courses))
    }

    fun getCourseInfo(id: Long) = viewModelScope.launch {
        repository.getCourseInfo(id)
            .onStart { _courseInfo.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _courseInfo.postValue((Resource.Error(context.getString(R.string.no_connection))))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _courseInfo.postValue((Resource.Error(context.getString(R.string.server_error))))
                        else
                            _courseInfo.postValue((Resource.Error(context.getString(R.string.connection_error))))
                    }
                    else -> {
                        _courseInfo.postValue((Resource.Error(context.getString(R.string.unknown_error))))
                    }
                }
            }
            .collect {
                _courseInfo.postValue(Resource.Success(it))
            }
    }

    fun getCourseSupscription(subscriptionType: String, courseId: Long) = viewModelScope.launch {
        repository.getCourseSubscription(subscriptionType, courseId)
            .onStart { _courseSubscription.postValue((Resource.Loading())) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _courseSubscription.postValue((Resource.Error(context.getString(R.string.no_connection))))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _courseSubscription.postValue((Resource.Error(context.getString(R.string.server_error))))
                        else
                            _courseSubscription.postValue((Resource.Error(context.getString(R.string.connection_error))))
                    }
                    else -> {
                        _courseSubscription.postValue((Resource.Error(context.getString(R.string.unknown_error))))
                    }
                }
            }
            .collect {
                _courseSubscription.postValue(Resource.Success(it))
            }
    }

    fun saveCourse(course: Course) = viewModelScope.launch {
        repository.addCourse(course)
    }

    fun deleteCourse(course: Course) = viewModelScope.launch {
        repository.deleteCourse(course)
    }
}