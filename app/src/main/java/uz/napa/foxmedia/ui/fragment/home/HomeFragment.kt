package uz.napa.foxmedia.ui.fragment.home

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.annotation.RawRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_home.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.api.RetrofitInstanceBearer
import uz.napa.foxmedia.db.DatabaseProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.repository.TokenProvider
import uz.napa.foxmedia.request.register.SignInRequest
import uz.napa.foxmedia.ui.activity.RegisterActivity
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.home.adapter.CategoryAdapter
import uz.napa.foxmedia.ui.fragment.home.adapter.TopCoursesAdapter
import uz.napa.foxmedia.ui.fragment.login.ANDROID
import uz.napa.foxmedia.util.Constants
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import uz.napa.foxmedia.util.Constants.Companion.FIRST_NAME
import uz.napa.foxmedia.util.Constants.Companion.LAST_NAME
import uz.napa.foxmedia.util.Constants.Companion.PASSWORD
import uz.napa.foxmedia.util.Constants.Companion.PHONE_NUM
import uz.napa.foxmedia.util.Constants.Companion.SESSION_ID
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory
import uz.napa.foxmedia.util.snackbar

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val homeViewModel by viewModels<HomeViewModel> {
        getViewModelFactory(
            MyRepository(
                DatabaseProvider.invoke(requireContext())
            )
        )
    }
    private val categoryAdapter by lazy { CategoryAdapter() }
    private val topCourseAdapter by lazy { TopCoursesAdapter() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpRv()
        setUpListeners()
        requireActivity().window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        homeViewModel.categories.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    hideCategories(true)
                    hideCourses(true)
                    startLottieAnimation(R.raw.loading_list)
                }
                is Resource.Error -> {
                    hideCategories(true)
                    hideCourses(true)
                    swipe_refresh_home.isRefreshing = false
                    startLottieAnimation(R.raw.no_connection)
                    snackbar(it.message!!)
                }
                is Resource.Success -> {
                    homeViewModel.getTopCourses()
                    val categories = it.data!!.categories
                    if (categories.isEmpty())
                        tv_categories.text = getString(R.string.no_categories)
                    else {
                        categoryAdapter.submitList(categories)
                        tv_categories.isVisible = true
                    }
                }
            }
        })

        homeViewModel.topCourses.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    startLottieAnimation(R.raw.loading_list)
                }
                is Resource.Error -> {
                    hideCategories(false)
                    snackbar(it.message!!)
                }
                is Resource.Success -> {
                    hideProgress()
                    val topCourses = it.data!!.courses
                    if (topCourses.isNotEmpty()) {
                        hideCourses(false)
                        topCourseAdapter.differ.submitList(topCourses)
                    }
                    hideCategories(false)
                }
            }
        })

        homeViewModel.userInfo.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { userResponse ->
                        Glide.with(this)
                            .load(BASE_URL + userResponse.user.thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .placeholder(R.drawable.gerb)
                            .into(img_user_avatar)
                        val pref = requireActivity().getSharedPreferences(
                            Constants.PREF_NAME,
                            Context.MODE_PRIVATE
                        )
                        if (!(pref.contains(FIRST_NAME) or pref.contains(LAST_NAME))) {
                            val editor = pref.edit()
                            editor.putString(FIRST_NAME, userResponse.user.firstName)
                            editor.putString(LAST_NAME, userResponse.user.lastName)
                            editor.apply()
                        }
                    }

                }

                is Resource.Loading -> {

                }

                is Resource.Error -> {
                    swipe_refresh_home.isRefreshing = true
                    img_user_avatar.setImageResource(R.drawable.gerb)
                    if (it.message == getString(R.string.log_out)) {
                        val pref =
                            requireActivity().getSharedPreferences(
                                Constants.PREF_NAME,
                                Context.MODE_PRIVATE
                            )
                        val phoneNum = pref.getLong(PHONE_NUM, 0)
                        val password = pref.getString(PASSWORD, "")
                        val sessionId = pref.getString(SESSION_ID, "")
                        val deviceType =
                            if (resources.getBoolean(R.bool.isTablet)) "TABLET" else "MOBILE"
                        homeViewModel.login(
                            SignInRequest(
                                phoneNum.toString(),
                                password.toString(),
                                sessionId,
                                Build.VERSION.RELEASE,
                                deviceType,
                                ANDROID,
                                Build.MODEL
                            )
                        )
                    }
                }
            }
        })

        homeViewModel.signInLiveData.observe(viewLifecycleOwner, Observer { response ->
            val pref =
                requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
            when (response) {
                is Resource.Error -> {
                    pref.clear()
                    pref.apply()
                    homeViewModel.clearDatabase()
                    TokenProvider.resetToken()
                    RetrofitInstanceBearer.instance = null
                    swipe_refresh_home.isRefreshing = false
                    snackbar(response.message.toString())
                    val intent = Intent(
                        requireContext(),
                        RegisterActivity::class.java
                    )
                    startActivity(intent)
                    requireActivity().finish()
                }
                is Resource.Success -> {
                    swipe_refresh_home.isRefreshing = false
                    val data = response.data!!
                    pref.putString(SESSION_ID, data.session.id)
                    pref.putLong(Constants.USER_ID, data.session.userId)
                    pref.putString(Constants.TOKEN, data.token)
                    pref.apply()
                }
            }
        })
    }

    private fun setUpListeners() {
        et_search.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_searchFragment)
        }

        swipe_refresh_home.setOnRefreshListener {
            homeViewModel.getCategories()
        }

        categoryAdapter.setOnClickListener {
            val actions = HomeFragmentDirections.actionNavHomeToCategoryFragment(it.id)
            findNavController().navigate(actions)
        }

        topCourseAdapter.setOnClickListener {
            val action = HomeFragmentDirections.actionNavHomeToCourseFragment(it)
            if (findNavController().currentDestination?.id == R.id.nav_home)
                findNavController().navigate(action)
        }
    }

    private fun setUpRv() {
        rv_courses.apply {
            adapter = topCourseAdapter
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        }
        rv_categories.apply {
            adapter = categoryAdapter
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        }
    }

    private fun hideCategories(isVisible: Boolean) {
        tv_categories.isVisible = !isVisible
        tv_courses.isVisible = !isVisible
    }

    private fun hideCourses(isVisible: Boolean) {
        tv_courses.isVisible = !isVisible
        rv_courses.isVisible = !isVisible
    }

    private fun startLottieAnimation(@RawRes resAnim: Int) {
        lottie_loading.isVisible = true
        lottie_loading.setAnimation(resAnim)
        lottie_loading.playAnimation()
    }


    private fun hideProgress() {
        swipe_refresh_home.isRefreshing = false
        lottie_loading.isVisible = false
    }


}
