package uz.napa.foxmedia.util

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.ViewModelFactory
import uz.napa.foxmedia.ViewModelFactoryWithParametrs
import uz.napa.foxmedia.repository.MyRepository
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun View.hideKeyboard(): Boolean {
    try {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) {
    }
    return false
}

fun View.showSoftKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(
        InputMethodManager.SHOW_FORCED,
        0
    )

}

fun Fragment.getViewModelFactory(repository: MyRepository): ViewModelFactory {
    return ViewModelFactory(repository)
}

fun Fragment.getViewModelFactory(
    repository: MyRepository,
    id: Long
): ViewModelFactoryWithParametrs {
    return ViewModelFactoryWithParametrs(repository, id)
}

fun AppCompatActivity.getViewModelFactory(
    repository: MyRepository,
    id: Long
): ViewModelFactoryWithParametrs {
    return ViewModelFactoryWithParametrs(repository, id)
}

fun Fragment.snackbar(message: String) {
    Snackbar.make(this.requireView(), message, Snackbar.LENGTH_SHORT).show()
}

fun AppCompatActivity.snackbar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}

fun formatDuration(totalDuration: Long): String {
    val secons = totalDuration % 60
    var hour = totalDuration / 60
    val minutes = hour % 60
    hour /= 60
    return if (hour % 100 >= 1) String.format(
        "%03d:%02d:%02d",
        hour,
        minutes,
        secons
    ) else String.format("%02d:%02d:%02d", hour, minutes, secons)
}

fun formatMoney(price: Any): String {
    return DecimalFormat("###,###,###,###").format(price)
}

fun htmlFormat(text: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(text)
    }
}

fun getRating(rating: Int): Float {
    return rating.toFloat() / 20
}

fun formatDate(time: String): String {
    val date = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    ).parse(time)

    return if (date != null)
        SimpleDateFormat("dd.MM.YYYY HH:mm:ss", Locale.getDefault()).format(date)
    else "00"
}

fun handleError(error: Throwable): String {
    val context = App.appInstance
    return when (error) {
        is IOException -> {
            context.getString(R.string.no_connection)
        }
        is HttpException -> {
            when {
                error.code() == 500 -> context.getString(R.string.server_error)
                error.code() == 401 -> context.getString(R.string.log_out)
                else -> context.getString(R.string.connection_error)
            }
        }
        else -> {
            context.getString(R.string.unknown_error)
        }
    }
}

