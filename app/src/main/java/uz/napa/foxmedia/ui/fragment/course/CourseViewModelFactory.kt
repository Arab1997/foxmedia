package uz.napa.foxmedia.ui.fragment.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.ui.fragment.category.CategoryViewModel
import uz.napa.foxmedia.ui.fragment.subcategory.SubcategoryViewModel

class CourseViewModelFactory(private val repository: MyRepository, private val course: Course) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(CourseViewModel::class.java) -> CourseViewModel(repository,course)
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}