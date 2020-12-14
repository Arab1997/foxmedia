package uz.napa.foxmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.ui.activity.video.VideoViewModel
import uz.napa.foxmedia.ui.fragment.account.AccountViewModel
import uz.napa.foxmedia.ui.fragment.home.HomeViewModel
import uz.napa.foxmedia.ui.fragment.login.LoginViewModel
import uz.napa.foxmedia.ui.fragment.login.confirm.ConfirmViewModel
import uz.napa.foxmedia.ui.fragment.login.register.RegisterViewModel
import uz.napa.foxmedia.ui.fragment.login.reset.ResetPasswordVM
import uz.napa.foxmedia.ui.fragment.login.restore.RestoreViewModel
import uz.napa.foxmedia.ui.fragment.mycourses.MyCourseViewModel
import uz.napa.foxmedia.ui.fragment.purchase.PurchaseViewModel
import uz.napa.foxmedia.ui.fragment.search.SearchViewModel
import uz.napa.foxmedia.ui.fragment.wishlist.WishListViewModel

class ViewModelFactory(private val repository: MyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository)
                isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository)
                isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(repository)
                isAssignableFrom(ConfirmViewModel::class.java) -> ConfirmViewModel(repository)
                isAssignableFrom(RestoreViewModel::class.java) -> RestoreViewModel(repository)
                isAssignableFrom(ResetPasswordVM::class.java) -> ResetPasswordVM(repository)
                isAssignableFrom(AccountViewModel::class.java) -> AccountViewModel(repository)
                isAssignableFrom(WishListViewModel::class.java) -> WishListViewModel(repository)
                isAssignableFrom(MyCourseViewModel::class.java) -> MyCourseViewModel(repository)
                isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(repository)
                isAssignableFrom(PurchaseViewModel::class.java) -> PurchaseViewModel(repository)
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}