package uz.napa.foxmedia.ui.fragment.home

import androidx.lifecycle.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.SignInRequest
import uz.napa.foxmedia.response.category.CategoryResponse
import uz.napa.foxmedia.response.course.TopCoursesResponse
import uz.napa.foxmedia.response.sign_in.SignInResponse
import uz.napa.foxmedia.response.user.UserInfoResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class HomeViewModel(private val repository: MyRepository) : ViewModel() {

    private val _categories = MutableLiveData<Resource<CategoryResponse>>()
    val categories: LiveData<Resource<CategoryResponse>> = _categories


    private val _topCourses = MutableLiveData<Resource<TopCoursesResponse>>()
    val topCourses: LiveData<Resource<TopCoursesResponse>> = _topCourses

    private val _userInfo = MutableLiveData<Resource<UserInfoResponse>>()
    val userInfo: LiveData<Resource<UserInfoResponse>> = _userInfo

    private val _signInLiveData = MutableLiveData<Resource<SignInResponse>>()
    val signInLiveData: LiveData<Resource<SignInResponse>> = _signInLiveData

    init {
        getCategories()
        getUserInfo()
    }

    fun clearDatabase() = viewModelScope.launch {
        repository.clearDatabase()
    }

    fun getCategories() = viewModelScope.launch {
        repository.getCategory()
            .onStart { _categories.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _categories.postValue(Resource.Error(context.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _categories.postValue(Resource.Error(context.getString(R.string.server_error)))
                        else
                            _categories.postValue(Resource.Error(context.getString(R.string.connection_error)))
                    }
                    else -> {
                        _categories.postValue(Resource.Error(context.getString(R.string.unknown_error)))
                    }
                }
            }
            .collect {
                _categories.postValue(Resource.Success(it))
            }
    }


    fun getTopCourses() = viewModelScope.launch {
        repository.getTopCourses()
            .onStart { _topCourses.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _topCourses.postValue(Resource.Error(context.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _topCourses.postValue(Resource.Error(context.getString(R.string.server_error)))
                    }
                    else -> {
                        _topCourses.postValue(Resource.Error(context.getString(R.string.unknown_error)))
                    }
                }
            }
            .collect {
                _topCourses.postValue(Resource.Success(it))
            }
    }

    fun getUserInfo() = viewModelScope.launch {
        repository.getUserInfo()
            .onStart { _userInfo.postValue(Resource.Loading()) }
            .catch { _userInfo.postValue(Resource.Error(handleError(it))) }
            .collect { _userInfo.postValue(Resource.Success(it)) }
    }

    fun login(signInRequest: SignInRequest) = viewModelScope.launch {
        repository.login(signInRequest)
            .onStart {
                _signInLiveData.postValue(Resource.Loading())
            }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _signInLiveData.postValue(Resource.Error(context.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        when {
                            it.code() == 400 -> {
                                _signInLiveData.postValue(Resource.Error(context.getString(R.string.invalid_user)))
                            }
                            it.code() == 500 -> {
                                _signInLiveData.postValue(Resource.Error(context.getString(R.string.server_error)))
                            }
                            else -> {
                                _signInLiveData.postValue(Resource.Error(context.getString(R.string.connection_error)))
                            }
                        }
                    }
                    else -> _signInLiveData.postValue(Resource.Error(context.getString(R.string.unknown_error)))
                }
            }
            .collect {
                _signInLiveData.postValue(Resource.Success(it))
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