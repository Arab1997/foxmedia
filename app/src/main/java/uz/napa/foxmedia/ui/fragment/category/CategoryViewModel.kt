package uz.napa.foxmedia.ui.fragment.category

import androidx.lifecycle.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.category.Category
import uz.napa.foxmedia.response.category.Subcategory
import uz.napa.foxmedia.response.category.SubcategoryResponse
import uz.napa.foxmedia.response.category.topcourse.CategoryTopCourseResponse
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.ui.fragment.category.adapter.SubCategoryTopCoursesModel
import uz.napa.foxmedia.util.Event
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class CategoryViewModel(private val repository: MyRepository,private val id:Long) : ViewModel() {

    private val _subcategoryViewModel = MutableLiveData<Resource<SubcategoryResponse>>()
    val subcategoryResponse: LiveData<Resource<SubcategoryResponse>> = _subcategoryViewModel

    private val _categoryCourses = MutableLiveData<Resource<CategoryTopCourseResponse>>()
    val categoryTopCourses: LiveData<Resource<CategoryTopCourseResponse>> = _categoryCourses

    var subcategory:List<Category>? = null

    init {
        getSubcategories(id)
    }

    fun getTopCourses() = viewModelScope.launch {
        repository.getCategoriesTopCourse()
            .onStart { _categoryCourses.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _categoryCourses.postValue(Resource.Error(context.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _categoryCourses.postValue(Resource.Error(context.getString(R.string.server_error)))
                        else
                            _categoryCourses.postValue(Resource.Error(context.getString(R.string.connection_error)))
                    }
                    else -> {
                        _categoryCourses.postValue(Resource.Error(context.getString(R.string.unknown_error)))
                    }
                }
            }
            .collect { _categoryCourses.postValue(Resource.Success(it)) }
    }

    fun getSubcategories(id: Long) = viewModelScope.launch {
        repository.getSubCategories(id)
            .onStart { _subcategoryViewModel.postValue((Resource.Loading())) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _subcategoryViewModel.postValue((Resource.Error(context.getString(R.string.no_connection))))
                    }
                    is HttpException -> {
                        if (it.code() == 500)
                            _subcategoryViewModel.postValue((Resource.Error(context.getString(R.string.server_error))))
                        else

                            _subcategoryViewModel.postValue((Resource.Error(context.getString(R.string.connection_error))))
                    }
                    else -> {
                        _subcategoryViewModel.postValue((Resource.Error(context.getString(R.string.unknown_error))))
                    }
                }
            }
            .collect {
                _subcategoryViewModel.postValue((Resource.Success(it)))
            }
    }

    private val _sortedTopCourses = MutableLiveData<ArrayList<SubCategoryTopCoursesModel>>()
    val sortedTopCourses: LiveData<ArrayList<SubCategoryTopCoursesModel>> = _sortedTopCourses

    fun sortTopCourses(categories: ArrayList<Subcategory>, topCourses: ArrayList<Course>) =
        viewModelScope.launch {
            val subCategList = ArrayList<SubCategoryTopCoursesModel>()
            categories.forEach { subCateg ->
                val tempCourse = ArrayList<Course>()
                topCourses.forEach { course ->
                    if (subCateg.id == course.categoryId) {
                        tempCourse.add(course)
                    }
                }
                subCategList.add(
                    SubCategoryTopCoursesModel(
                        subCateg,
                        tempCourse
                    )
                )
            }

            _sortedTopCourses.postValue(subCategList)
        }
}