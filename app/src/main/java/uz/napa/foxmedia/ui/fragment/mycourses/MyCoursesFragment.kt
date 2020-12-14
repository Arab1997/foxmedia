package uz.napa.foxmedia.ui.fragment.mycourses

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my_courses.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.db.DatabaseProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.response.user.completed_course.CompletedCourse
import uz.napa.foxmedia.response.user.subscription.UserSubscription
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.mycourses.adapter.CompletedCourseAdapter
import uz.napa.foxmedia.ui.fragment.mycourses.adapter.PurchasedCourseAdapter
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory

class MyCoursesFragment : BaseFragment(R.layout.fragment_my_courses) {
    private val myCourseVM by viewModels<MyCourseViewModel> {
        getViewModelFactory(
            MyRepository(
                DatabaseProvider.invoke(requireContext())
            )
        )
    }
    private val purchasedCourseAdapter by lazy { PurchasedCourseAdapter() }
    private val completedCourseAdapter by lazy { CompletedCourseAdapter() }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpListeners()
        myCourseVM.userCourses.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { userSubscriptionResponse ->
                        purchasedCourseAdapter.differ.submitList(userSubscriptionResponse.userSubscriptions)
                    }
                }
            }
        })

        myCourseVM.completedCourseResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { userSubscriptionResponse ->
                        completedCourseAdapter.differ.submitList(userSubscriptionResponse.completedCourses)
                    }
                }
            }
        })

        myCourseVM.courseInfo.observe(viewLifecycleOwner, Observer {
            when (it.peekContent()) {
                is Resource.Loading -> {
                    progress_bar_course.isVisible = true
                }
                is Resource.Success -> {
                    it.getContentIfNotHandled()?.data?.let { courseRes ->
                        val action =
                            MyCoursesFragmentDirections.actionMyCoursesFragmentToCourseFragment(
                                courseRes.course
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        })
    }

    private fun setUpListeners() {
        tab_my_course.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    if (it.position == 0)
                        rv_my_course.adapter = purchasedCourseAdapter
                    if (it.position == 1)
                        rv_my_course.adapter = completedCourseAdapter
                }
            }

        })

        purchasedCourseAdapter.setOnCourseClickListener {
            myCourseVM.getCourseInfo(it.courseId ?: 0)
        }

        completedCourseAdapter.setOnCourseClickListener {
            myCourseVM.getCourseInfo(it.id)
        }
    }

    private fun setUpViews() {
        tab_my_course.addTab(tab_my_course.newTab().setText(getString(R.string.purchased)))
        tab_my_course.addTab(tab_my_course.newTab().setText(getString(R.string.completed)))
        rv_my_course.apply {
            adapter = purchasedCourseAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

}