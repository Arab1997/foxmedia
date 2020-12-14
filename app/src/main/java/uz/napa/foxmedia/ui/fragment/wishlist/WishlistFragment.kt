package uz.napa.foxmedia.ui.fragment.wishlist

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_wishlist.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.db.DatabaseProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.subcategory.adapter.CourseAdapter
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory
import kotlin.math.roundToInt


class WishlistFragment : BaseFragment(R.layout.fragment_wishlist) {
    private val wishListViewModel by viewModels<WishListViewModel> {
        getViewModelFactory(
            MyRepository(DatabaseProvider.invoke(requireContext()))
        )
    }
    private val adapterWishList by lazy { CourseAdapter() }
    val list = ArrayList<Course>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val course = list.removeAt(position)
                wishListViewModel.deleteCourse(course)
                adapterWishList.differ.submitList(list.toList())
                val snackbar = Snackbar.make(rv_wish_list, "Course Removed", Snackbar.LENGTH_SHORT)
                snackbar.setAction("Undo") {
                    wishListViewModel.addCourse(course)
                    list.add(position, course)
                    adapterWishList.differ.submitList(list.toList())
                    rv_wish_list.scrollToPosition(position)
                }
                snackbar.show()
            }
        }
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(rv_wish_list)
        rv_wish_list.apply {
            adapter = adapterWishList
            itemAnimator = DefaultItemAnimator()
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(),
                R.anim.layout_animation_fall_down
            )
        }
        wishListViewModel.wishList.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { list2 ->
                        adapterWishList.differ.submitList(list2)
                        list.addAll(adapterWishList.differ.currentList)
                    }
                }
            }
        })

        adapterWishList.setOnCourseClickListener {
            val action = WishlistFragmentDirections.actionWishlistFragmentToCourseFragment(it)
            findNavController().navigate(action)
        }
    }
}