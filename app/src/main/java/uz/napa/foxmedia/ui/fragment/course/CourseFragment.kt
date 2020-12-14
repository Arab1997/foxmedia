package uz.napa.foxmedia.ui.fragment.course

import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_course.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.db.DatabaseProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.ui.activity.video.COURSE_ID
import uz.napa.foxmedia.ui.activity.video.INSTRUCTOR
import uz.napa.foxmedia.ui.activity.video.VideoPlayerActivity
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.util.*
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL

const val ONE_TIME = "ONE-TIME"
const val FUNDED = "FUNDED"

class CourseFragment : BaseFragment(R.layout.fragment_course, isBottomNavVisible = false) {
    private val args by navArgs<CourseFragmentArgs>()
    private val courseViewModel by viewModels<CourseViewModel> {
        CourseViewModelFactory(MyRepository(DatabaseProvider.invoke(requireContext())), args.course)
    }
    private var isFavorite = false
    private var isCoursePurchased = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val course = args.course
        if (course.subscriptionType == ONE_TIME)
            courseViewModel.getCourseSupscription(course.subscriptionType, course.id)
        courseViewModel.getUserSubscription(course.subscriptionType ?: "")
        courseViewModel.getAllSavedCourses()
        courseViewModel.getCourseInfo(course.id)
        setViews(course)
        setUpListeners()

        courseViewModel.savedCourses.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    btn_favorite.isEnabled = false
                }
                is Resource.Success -> {
                    btn_favorite.isEnabled = true
                    val courses = it.data!!
                    if (courses.isNotEmpty()) {
                        courses.forEach { savedCourse ->
                            if (savedCourse.id == course.id) {
                                btn_favorite.setImageResource(R.drawable.ic_favorite_filled)
                                isFavorite = true
                                return@forEach
                            }
                        }
                    }
                }
                is Resource.Error -> {
                }
            }
        })

        courseViewModel.userSubscription.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    it.data?.let { response ->
                        val subscriptions = response.userSubscriptions
                        if (subscriptions.isNotEmpty()) {
                            if (args.course.subscriptionType == ONE_TIME) {
                                subscriptions.forEach { userSub ->
                                    if (userSub.courseId == args.course.id) {
                                        btn_purchase.setCardBackgroundColor(
                                            requireContext().resources.getColor(
                                                R.color.green
                                            )
                                        )
                                        tv_purchase.text =
                                            requireContext().getString(R.string.purchased)
                                        isCoursePurchased = true
                                        return@forEach
                                    } else {
                                        isCoursePurchased = false
                                        btn_purchase.setCardBackgroundColor(
                                            requireContext().resources.getColor(
                                                R.color.red
                                            )
                                        )
                                        tv_purchase.text =
                                            requireContext().getString(R.string.buy_now)
                                    }
                                }
                            } else {
                                isCoursePurchased = true
                                btn_purchase.setCardBackgroundColor(
                                    requireContext().resources.getColor(
                                        R.color.green
                                    )
                                )
                                tv_purchase.text = requireContext().getString(R.string.purchased)
                            }

                        } else {
                            btn_purchase.setCardBackgroundColor(
                                requireContext().resources.getColor(
                                    R.color.red
                                )
                            )
                            tv_purchase.text = requireContext().getString(R.string.buy_now)
                        }

                    }
                }
            }
        })


        courseViewModel.courseSubscription.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    it.data?.let { subscription ->
                        subscription.resource?.let { resourse ->
                            if (resourse.discount != null) {
                                tv_course_price.apply {
                                    text = getString(
                                        R.string.som_formatted,
                                        formatMoney(resourse.price)
                                    )
                                    paintFlags =
                                        tv_course_price.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                    setTextColor(resources.getColor(R.color.red))
                                }
                                tv_sale_price.apply {
                                    isVisible = true
                                    text = getString(
                                        R.string.som_formatted,
                                        formatMoney(resourse.saleprice)
                                    )
                                    setTextColor(resources.getColor(R.color.green))
                                }
                                tv_course_price_info.text =
                                    getString(R.string.som_formatted, formatMoney(resourse.price))
                                tv_discount.text = getString(R.string.percentage, resourse.discount)
                                tv_sale_price_info.text =
                                    getString(
                                        R.string.som_formatted,
                                        formatMoney(resourse.saleprice)
                                    )
                            } else {
                                tv_course_price_info.text =
                                    getString(R.string.som_formatted, formatMoney(resourse.price))
                                tv_course_price.text = resourse.price.toString()
                                tv_course_price.setTextColor(resources.getColor(R.color.colorPrimary))
                                tv_sale_price.isVisible = false
                            }
                        }
                    }
                }
            }
        })

        courseViewModel.courseInfo.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    tv_course_teacher.text = getString(R.string.loading)
                }
                is Resource.Error -> {
                    tv_course_teacher.text = getString(R.string.error)
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    it.data?.let { courseResponse ->
                        btn_play.setOnClickListener {
                            val intent = Intent(requireActivity(), VideoPlayerActivity::class.java)
                            intent.putExtra(COURSE_ID, args.course.id)
                            intent.putExtra(INSTRUCTOR, courseResponse.instructor)
                            startActivity(intent)
                        }
                        tv_course_teacher.text = courseResponse.course.instructorName
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tv_course_description.text =
                                Html.fromHtml(
                                    courseResponse.course.description,
                                    Html.FROM_HTML_MODE_COMPACT
                                )
                        } else {
                            tv_course_description.text =
                                Html.fromHtml(courseResponse.course.description)
                        }
                    }
                }
            }
        })
    }

    private fun setUpListeners() {
        btn_purchase.setOnClickListener {
            if (!isCoursePurchased) {
                val action =
                    CourseFragmentDirections.actionCourseFragmentToPurchaseFragment(args.course)
                if (findNavController().currentDestination?.id == R.id.courseFragment)
                    findNavController().navigate(action)
            }
        }
        btn_back.setOnClickListener { findNavController().popBackStack() }
        btn_favorite.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            animation.setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(p0: Animation?) {
                    super.onAnimationEnd(p0)
                    val course = args.course
                    if (isFavorite) {
                        btn_favorite.setImageResource(R.drawable.ic_favorite_outlined)
                        courseViewModel.deleteCourse(course)
                    } else {
                        btn_favorite.setImageResource(R.drawable.ic_favorite_filled)
                        courseViewModel.saveCourse(course)
                    }
                    isFavorite = !isFavorite
                }
            })
            it.startAnimation(animation)
        }
    }

    private fun setViews(course: Course) {
        tv_current_position.text = formatDuration(course.totalDuration)

        Glide.with(this).load(BASE_URL + course.image).placeholder(R.drawable.logo)
            .into(img_course)
        tv_course_name.text = course.name
        course_rating.rating = getRating(course.totalRating)
        tv_course_rating.text = getRating(course.totalRating).toString()
        tv_subscription_type.text = course.subscriptionType
        tv_best_seller.isVisible = course.isBestSeller == true
        tv_recommended.isVisible = course.isRecommended == true
        tv_videos_count.text = course.videosCount.toString()
    }


    private fun showViews() {
        layout_course_info.isVisible = true
    }

    private fun hideViews() {
        layout_course_info.isVisible = false
    }

}