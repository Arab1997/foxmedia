package uz.napa.foxmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.ui.activity.video.VideoViewModel
import uz.napa.foxmedia.ui.fragment.category.CategoryViewModel
import uz.napa.foxmedia.ui.fragment.subcategory.SubcategoryViewModel

class ViewModelFactoryWithParametrs(private val repository: MyRepository, private val id: Long) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(CategoryViewModel::class.java) -> CategoryViewModel(repository,id)
                isAssignableFrom(SubcategoryViewModel::class.java) -> SubcategoryViewModel(repository,id)
                isAssignableFrom(VideoViewModel::class.java) -> VideoViewModel(repository,id)
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}