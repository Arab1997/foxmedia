package uz.napa.foxmedia.ui.fragment.category

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.tab_category
import kotlinx.android.synthetic.main.list_subcategory.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.category.Category
import uz.napa.foxmedia.response.category.Subcategory
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.category.adapter.SubCategoryAdapter
import uz.napa.foxmedia.ui.fragment.category.adapter.SubCategoryTopCoursesModel
import uz.napa.foxmedia.util.*
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import kotlin.ranges.random


class CategoryFragment : BaseFragment(R.layout.fragment_category, R.color.colorPrimary, false) {
    private val vm: SavedStateViewModel by viewModels()
    private val subCategoriesList = ArrayList<Subcategory>()
    private val topCoursesList = ArrayList<Course>()
    private lateinit var category: Category
    private val adapter by lazy { SubCategoryAdapter() }
    private val categoryVM by viewModels<CategoryViewModel> {
        getViewModelFactory(
            MyRepository(),
            args.categoryId
        )
    }
    private val args by navArgs<CategoryFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view_pager.adapter = adapter
        registerObservers()
    }


    private fun setUpViews(list: ArrayList<SubCategoryTopCoursesModel>) {
        toolbar.title = category.name
        adapter.differ.submitList(list)

        TabLayoutMediator(tab_category, view_pager) { tab: TabLayout.Tab, i: Int ->
            tab.text = list[i].subCategory.name
            view_pager.setCurrentItem(tab.position, true)
        }.attach()

        Glide.with(requireActivity())
            .load(BASE_URL + category.image)
            .listener(object : SimpleRequestListener() {
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    img_header?.let {
                        zoomInAnimation(img_header)
                    }
                    return false
                }
            })
            .into(img_header)

        vm.getRVPosition()?.let {
            val rvPosition = it[0]
            val vpPosition = it[1]
            adapter.scrollPosition(rvPosition, vpPosition)
            view_pager.setCurrentItem(vpPosition, false)
        }
    }

    private fun isLoading(isLoading: Boolean) {
        lottie_category.isVisible = isLoading
        layout_views.isVisible = !isLoading
    }


    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        actionBar.title = category.name
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    fun zoomInAnimation(viewToAnimate: View) {
        val rndX = (0.1 * (2..8).random()).toFloat()
        val rndY = (0.1 * (2..8).random()).toFloat()
        val anim: Animation = ScaleAnimation(
            1f, 1.4f,  // Start and end values for the X axis scaling
            1f, 1.4f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, rndX,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, rndY  // Pivot point of Y scaling
        )
        anim.fillAfter = true // Needed to keep the result of the animation
        anim.duration = 10000
        anim.setAnimationListener(object : SimpleAnimationListener() {
            override fun onAnimationEnd(p0: Animation?) {
                super.onAnimationEnd(p0)
                zoomOutAnimation(viewToAnimate, rndX, rndY)
            }
        })
        viewToAnimate.startAnimation(anim)
    }

    fun zoomOutAnimation(viewToAnimate: View, pivotX: Float, pivotY: Float) {
        val animOut: Animation = ScaleAnimation(
            1.4f, 1f,  // Start and end values for the X axis scaling
            1.4f, 1f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, pivotX,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, pivotY
        )
        animOut.fillAfter = true // Needed to keep the result of the animation

        animOut.duration = 10000
        viewToAnimate.startAnimation(animOut)
        animOut.setAnimationListener(object : SimpleAnimationListener() {
            override fun onAnimationEnd(p0: Animation?) {
                super.onAnimationEnd(p0)
                zoomInAnimation(viewToAnimate)
            }
        })
    }

    private fun registerObservers() {

        categoryVM.subcategoryResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    isLoading(true)
                }
                is Resource.Error -> {
                    isLoading(false)
                    snackbar(it.message.toString())

                }
                is Resource.Success -> {
                    it.data?.let { subcateg ->
                        category = subcateg.category
                        setUpToolbar()
                        val subcategory = category.subcategories
                        subcategory?.let {
                            subCategoriesList.clear()

                            subCategoriesList.addAll(subcategory)
                        }
                        if (categoryVM.subcategory == null)
                            categoryVM.getTopCourses()
                    }

                }
            }
        })

        categoryVM.categoryTopCourses.observe(
            viewLifecycleOwner,
            Observer { it ->
                when (it) {
                    is Resource.Loading -> {
                        isLoading(true)
                    }
                    is Resource.Error -> {
                        isLoading(false)
                        snackbar(it.message.toString())
                    }
                    is Resource.Success -> {
                        var categories: List<Category>? = categoryVM.subcategory
                        if (categories == null) {
                            categories = it.data!!.categories
                            categoryVM.subcategory = it.data.categories
                        }
                        categories.forEach { cat ->
                            if (cat.id == args.categoryId) {
                                cat.courses?.let { courses ->
                                    topCoursesList.clear()
                                    topCoursesList.addAll(courses)
                                    return@forEach
                                }
                            }
                        }
                        categoryVM.sortTopCourses(subCategoriesList, topCoursesList)
                    }
                }
            })

        categoryVM.sortedTopCourses.observe(viewLifecycleOwner, Observer {
            isLoading(false)
            setUpViews(it)
        })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view_pager?.let {
            vm.setRVPosition(
                (rv_top_courses.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition(),
                it.currentItem
            )
        }

    }

    override fun onStop() {
        super.onStop()
        rv_top_courses?.let {
            vm.setRVPosition(
                (rv_top_courses.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition(),
                view_pager.currentItem
            )
        }
    }
}
