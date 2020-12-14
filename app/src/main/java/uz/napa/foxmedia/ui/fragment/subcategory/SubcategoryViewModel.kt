package uz.napa.foxmedia.ui.fragment.subcategory

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
import uz.napa.foxmedia.response.subcategory.CoursesByCategoryResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class SubcategoryViewModel(private val repository: MyRepository, private val id: Long) :
    ViewModel() {

    private val _courses = MutableLiveData<Resource<CoursesByCategoryResponse>>()
    val courses: LiveData<Resource<CoursesByCategoryResponse>>
        get() {
            return _courses
        }
    var coursePages = 1
    private var coursesByCategoryResponse: CoursesByCategoryResponse? = null

    init {
        getCourses(id)
    }

    fun getCourses(categoryId: Long) = viewModelScope.launch {
        repository.getCoursesByCategory(categoryId, coursePages)
            .onStart { _courses.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _courses.postValue((Resource.Error(context.getString(R.string.no_connection))))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _courses.postValue((Resource.Error(context.getString(R.string.server_error))))
                        else

                            _courses.postValue((Resource.Error(context.getString(R.string.connection_error))))
                    }
                    else -> {
                        _courses.postValue((Resource.Error(context.getString(R.string.unknown_error))))
                    }
                }
            }
            .collect {
                coursePages++
                if (coursesByCategoryResponse == null) {
                    coursesByCategoryResponse = it
                } else {
                    val oldList = coursesByCategoryResponse?.courses?.data
                    val newList = it.courses.data
                    oldList?.addAll(newList)
                }
                _courses.postValue(Resource.Success(coursesByCategoryResponse ?: it))
            }
    }
}