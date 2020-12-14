package uz.napa.foxmedia.ui.fragment.subcategory

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_subcategory.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.subcategory.adapter.CourseAdapter
import uz.napa.foxmedia.util.Constants.Companion.QUERY_PAGE_SIZE
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory
import uz.napa.foxmedia.util.snackbar

class SubcategoryFragment :
    BaseFragment(R.layout.fragment_subcategory, R.color.colorPrimary, isBottomNavVisible = false) {
    private val courseAdapter by lazy { CourseAdapter() }
    private val args by navArgs<SubcategoryFragmentArgs>()
    private val subcategoryVM by viewModels<SubcategoryViewModel> {
        getViewModelFactory(
            MyRepository(),
            args.subcategory.id
        )
    }

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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                subcategoryVM.getCourses(args.subcategory.id)
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
        setUpToolbar()
        setUpRv()
        setObservers()

    }

    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar_subcategory)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        actionBar.title = args.subcategory.name
        actionBar.setDisplayHomeAsUpEnabled(true)
        toolbar_subcategory.navigationIcon?.setColorFilter(
            resources.getColor(R.color.white),
            PorterDuff.Mode.SRC_ATOP
        );
        toolbar_subcategory.setTitleTextColor(resources.getColor(R.color.white))
    }

    private fun setObservers() {
        subcategoryVM.courses.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { courses ->
                        hideLoading()
                        if (courses.courses.data.isEmpty()) {
                            tv_no_courses.isVisible = true
                            rv_subcategory.isVisible = false
                            progress_subcategory.isVisible = false
                        } else {
                            courseAdapter.differ.submitList(courses.courses.data)
                            val totalPages = courses.courses.total / QUERY_PAGE_SIZE + 2
                            isLastPage = subcategoryVM.coursePages == totalPages
                        }

                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    snackbar(it.message.toString())
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })
    }

    private fun setUpRv() {
        rv_subcategory.apply {
            adapter = courseAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(),
                R.anim.layout_animation_fall_down
            )
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(rvOnScrollListener)
        }
        courseAdapter.setOnCourseClickListener {
            val action = SubcategoryFragmentDirections.actionSubcategoryFragmentToCourseFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun showLoading() {
        progress_subcategory.isVisible = true
        isLoading = true
    }

    private fun hideLoading() {
        progress_subcategory.isVisible = false
        isLoading = false
    }


}