package uz.napa.foxmedia.ui.fragment.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_search.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.search.SearchRequest
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.search.adapter.SearchCourseAdapter
import uz.napa.foxmedia.util.*


class SearchFragment : BaseFragment(R.layout.fragment_search, isBottomNavVisible = false) {
    private val searchVM by viewModels<SearchViewModel> { getViewModelFactory(MyRepository()) }
    private val searchAdapter by lazy { SearchCourseAdapter() }

    private var isLoading = false
    private var isScrolling = false
    var isLastPage = false
    private val rvOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                performSearch(et_search.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSearching()
        setUpRv()
        setListeners()
        searchVM.searchCourse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { response ->
                        searchAdapter.differ.submitList(response.results.data)
                        val totalPages = response.results.total / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = searchVM.searchPages == totalPages
                    }
                }
            }
        })
    }

    private fun showProgress() {
        search_progress.isVisible = true
    }

    private fun hideProgress() {
        search_progress.isVisible = false
    }

    private fun setUpRv() {
        rv_search.apply {
            adapter = searchAdapter
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(rvOnScrollListener)
        }
    }

    private fun setListeners() {
        searchAdapter.setOnCourseClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToCourseFragment(it)
            findNavController().navigate(action)
            et_search.hideKeyboard()
        }
        btn_back.setOnClickListener { exitFragment() }
        et_search.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchVM.searchResponse = null
                performSearch(textView.text.toString())
                et_search.hideKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun performSearch(query: String) {
        searchVM.searchCourse(SearchRequest(query))
    }

    private fun startSearching() {
        et_search.requestFocus()
        val imgr: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private fun exitFragment() {
        et_search.clearFocus()
        et_search.hideKeyboard()
        findNavController().popBackStack()
    }


}