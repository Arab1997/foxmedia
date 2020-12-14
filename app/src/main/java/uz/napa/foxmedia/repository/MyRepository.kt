package uz.napa.foxmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Part
import uz.napa.foxmedia.App
import uz.napa.foxmedia.api.RetrofitInstance
import uz.napa.foxmedia.api.RetrofitInstanceBearer
import uz.napa.foxmedia.db.DatabaseProvider
import uz.napa.foxmedia.request.analytics.WatchTimeRequest
import uz.napa.foxmedia.request.comment.CreateCommentRequest
import uz.napa.foxmedia.request.comment.EditCommentRequest
import uz.napa.foxmedia.request.purchase.PurchaseRequest
import uz.napa.foxmedia.request.register.*
import uz.napa.foxmedia.request.search.SearchRequest
import uz.napa.foxmedia.request.user.ChangePasswordRequest
import uz.napa.foxmedia.request.user.UpdateUsernameRequest
import uz.napa.foxmedia.response.category.CategoryResponse
import uz.napa.foxmedia.response.category.SubcategoryResponse
import uz.napa.foxmedia.response.category.topcourse.CategoryTopCourseResponse
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.response.course.CourseInfoResponse
import uz.napa.foxmedia.response.course.TopCoursesResponse
import uz.napa.foxmedia.response.course.subscription.CourseSubscriptionResponse
import uz.napa.foxmedia.response.regions.BaseProvincesResponse
import uz.napa.foxmedia.response.regions.Province
import uz.napa.foxmedia.response.regions.Region
import uz.napa.foxmedia.response.register.ConfirmResponse
import uz.napa.foxmedia.response.register.RegisterResponse
import uz.napa.foxmedia.response.register.SendTokenResponse
import uz.napa.foxmedia.response.register.VerifyTokenResponse
import uz.napa.foxmedia.response.sign_in.SignInResponse
import uz.napa.foxmedia.response.subcategory.CoursesByCategoryResponse
import uz.napa.foxmedia.util.Constants.Companion.PATCH
import uz.napa.foxmedia.util.MyPreference

class MyRepository(db: DatabaseProvider? = null) {
    private val api by lazy { RetrofitInstance.api }
    private val dao = db?.getDatabaseDao()
    private val bearerApi by lazy { RetrofitInstanceBearer.instance(TokenProvider.getToken(App.appInstance)) }
    private val lang = getLang()

    private fun getLang(): String {
        return MyPreference(App.appInstance).getLang()
    }

    suspend fun login(signInRequest: SignInRequest): Flow<SignInResponse> = flow {
        emit(api.login(signInRequest))
    }

    suspend fun getProvince(): Flow<BaseProvincesResponse<Province>> = flow {
        emit(api.getProvinces(lang))
    }

    suspend fun getRegions(provinceId: Long): Flow<BaseProvincesResponse<Region>> = flow {
        emit(api.getRegions(provinceId, lang))
    }

    suspend fun register(registerRequest: RegisterRequest): Flow<RegisterResponse> = flow {
        emit(api.register(registerRequest))
    }

    suspend fun confirm(confirmRequest: ConfirmRequest): Flow<ConfirmResponse> = flow {
        emit(api.confirmNumber(confirmRequest))
    }

    suspend fun sendToken(sendTokenRequest: SendTokenRequest): Flow<SendTokenResponse> = flow {
        emit(api.sendToken(sendTokenRequest))
    }

    suspend fun verifyToken(confirmRequest: ConfirmRequest): Flow<VerifyTokenResponse> = flow {
        emit(api.verifyToken(confirmRequest))
    }

    suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest): Flow<ConfirmResponse> =
        flow {
            emit(api.resetPassword(resetPasswordRequest))
        }

    suspend fun getCategory(): Flow<CategoryResponse> = flow {
        emit(api.getCategories(lang))
    }

    suspend fun getTopCourses(): Flow<TopCoursesResponse> = flow {
        emit(api.getTopCourses(lang))
    }

    suspend fun getSubCategories(id: Long): Flow<SubcategoryResponse> = flow {
        emit(api.getSubcategories(id, lang))
    }

    suspend fun getCategoriesTopCourse(): Flow<CategoryTopCourseResponse> = flow {
        emit(api.getCategoriesTopCourses(lang))
    }

    suspend fun getCoursesByCategory(
        categoryId: Long,
        pageNumber: Int
    ): Flow<CoursesByCategoryResponse> = flow {
        emit(api.getCoursesbyCategory(categoryId, pageNumber, lang))
    }

    suspend fun getCourseInfo(courseId: Long): Flow<CourseInfoResponse> = flow {
        emit(api.getCourseInfo(courseId, lang))
    }

    suspend fun getCourseSubscription(
        subscriptionType: String,
        courseId: Long
    ): Flow<CourseSubscriptionResponse> = flow {
        emit(api.getSupscriptionType(subscriptionType, courseId, lang))
    }

    suspend fun getUserInfo() = flow {
        emit(bearerApi.getUserInfo(lang))
    }

    suspend fun getCourseVideos(courseId: Long) = flow {
        emit(api.getCourseVideos(courseId))
    }

    suspend fun getUserSubscriptions(subscriptionType: String) = flow {
        emit(bearerApi.getUserSubscriptions(subscriptionType))
    }

    suspend fun getUserTransaction() = flow {
        emit(bearerApi.getUserTransactions())
    }

    suspend fun getUserSessions() = flow {
        emit(bearerApi.getUserSessions())
    }

    suspend fun getCompletedCourses() = flow {
        emit(bearerApi.getCompletedCourses(lang))
    }

    suspend fun changeUserImage(@Part image: MultipartBody.Part) = flow {
        val method = PATCH.toRequestBody("text/plain".toMediaTypeOrNull())
        emit(bearerApi.changeImage(image, method))
    }

    suspend fun changeUsername(updateUsernameRequest: UpdateUsernameRequest) = flow {
        emit(bearerApi.changeUsername(updateUsernameRequest, lang))
    }

    suspend fun changePassword(updatePasswordRequest: ChangePasswordRequest) = flow {
        emit(bearerApi.changePassword(updatePasswordRequest))
    }

    suspend fun searchCourses(searchRequest: SearchRequest, page: Int = 1) = flow {
        emit(api.searchCourse(searchRequest, page, lang))
    }

    suspend fun getVideoInfo(videoId: Long) = flow {
        emit(bearerApi.getVideoInfo(videoId, lang))
    }

    suspend fun createComment(createCommentRequest: CreateCommentRequest) = flow {
        emit(bearerApi.createComment(createCommentRequest))
    }

    suspend fun getVideoComments(videoId: Long, page: Int) = flow {
        emit(api.getVideoComments(videoId, page))
    }


    suspend fun getVideoFiles(videoId: Long) = flow {
        emit(bearerApi.getVideoFiles(videoId))
    }

    suspend fun deleteComment(commentId: Long) = flow {
        emit(bearerApi.deleteComment(commentId))
    }

    suspend fun editComment(commentId: Long, commentRequest: EditCommentRequest) = flow {
        emit(bearerApi.editComment(commentId, commentRequest))
    }

    suspend fun addVideoToHistory(videoId: Long) = flow {
        emit(bearerApi.addVideoToHistory(videoId))
    }

    suspend fun watchTimeAnalytics(watchTimeRequest: WatchTimeRequest) = flow {
        emit(bearerApi.watchTime(watchTimeRequest))
    }

    suspend fun getSubscriptionTable() = flow {
        emit(bearerApi.subscriptionsTable())
    }


    suspend fun getPurchaseDetails(subscriptionType: String, resourceId: Long) = flow {
        emit(bearerApi.subscriptionDetails(subscriptionType, resourceId, lang))
    }

    suspend fun purchaseSubscription(purchaseRequest: PurchaseRequest) = flow {
        emit(bearerApi.purchaseSubscription(purchaseRequest))
    }

    suspend fun checkoutBalance(checkoutType: String) = flow {
        emit(bearerApi.checkoutBalance(checkoutType, lang))
    }

    suspend fun addCourse(course: Course) = dao?.insertCourse(course)

    suspend fun deleteCourse(course: Course) = dao?.deleteCourse(course)

    suspend fun getAllWishCourses(): List<Course>? = dao?.getAllCourses()

    suspend fun clearDatabase() = dao?.nukeTable()
}