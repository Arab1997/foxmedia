package uz.napa.foxmedia.ui.fragment.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SavedStateViewModel(private val state: SavedStateHandle) : ViewModel() {

    fun setViewPagerPosition(key: String, value: Int) {
        state.set(key, value)
    }

    fun setRVPosition(value: Int, viewPagerPosition: Int) {
        state.set("RV", arrayOf(value, viewPagerPosition))
    }

    fun getRVPosition() = state.get<Array<Int>>("RV")


    fun getViewPagerPosition(key: String) = state.get<Int>(key)

}