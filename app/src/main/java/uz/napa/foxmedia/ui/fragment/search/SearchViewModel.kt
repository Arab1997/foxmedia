package uz.napa.foxmedia.ui.fragment.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.search.SearchRequest
import uz.napa.foxmedia.response.search.SearchResponse
import uz.napa.foxmedia.util.Resource

class SearchViewModel(private val repository: MyRepository) : ViewModel() {

    private val _searchCourse = MutableLiveData<Resource<SearchResponse>>()
    val searchCourse: LiveData<Resource<SearchResponse>> = _searchCourse
    var searchPages = 1
    var searchResponse: SearchResponse? = null

    fun searchCourse(searchRequest: SearchRequest) = viewModelScope.launch {
        repository.searchCourses(searchRequest)
            .onStart { _searchCourse.postValue(Resource.Loading()) }
            .catch {
                _searchCourse.postValue(Resource.Error(it.message.toString()))
            }
            .collect {
                if (searchResponse == null) {
                    searchResponse = it
                } else {
                    searchPages++
                    val oldList = searchResponse?.results?.data
                    val newList = it.results.data
                    oldList?.addAll(newList)
                }
                _searchCourse.postValue(Resource.Success(searchResponse ?: it))
            }
    }
}