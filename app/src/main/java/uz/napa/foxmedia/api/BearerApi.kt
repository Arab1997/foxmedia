package uz.napa.foxmedia.api

import uz.napa.foxmedia.response.user.ChangePasswordResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.http.*
import uz.napa.foxmedia.request.analytics.WatchTimeRequest
import uz.napa.foxmedia.request.comment.CreateCommentRequest
import uz.napa.foxmedia.request.comment.EditCommentRequest
import uz.napa.foxmedia.request.purchase.PurchaseRequest
import uz.napa.foxmedia.request.user.ChangePasswordRequest
import uz.napa.foxmedia.request.user.UpdateUsernameRequest
import uz.napa.foxmedia.response.analytics.WatchHistoryResponse
import uz.napa.foxmedia.response.analytics.WatchTimeResponse
import uz.napa.foxmedia.response.purchase.CheckoutResponse
import uz.napa.foxmedia.response.purchase.PurchaseResponse
import uz.napa.foxmedia.response.purchase.SubscriptionDetailsResponse
import uz.napa.foxmedia.response.purchase.SubscriptionsTable
import uz.napa.foxmedia.response.user.UserInfoResponse
import uz.napa.foxmedia.response.user.completed_course.CompletedCourseResponse
import uz.napa.foxmedia.response.user.image.ChangeImageResponse
import uz.napa.foxmedia.response.user.session.SessionsResponse
import uz.napa.foxmedia.response.user.subscription.UserSubscriptionResponse
import uz.napa.foxmedia.response.user.transaction.TransactionResponse
import uz.napa.foxmedia.response.video.VideoInfoResponse
import uz.napa.foxmedia.response.video.comment.CreateCommentResponse
import uz.napa.foxmedia.response.video.comment.EditCommentResponse
import uz.napa.foxmedia.response.video.files.VideoFilesResponse

interface BearerApi {
    @GET("/users/profile")
    suspend fun getUserInfo(@Query("_lang") lang: String): UserInfoResponse

    @GET("users/subscriptions")
    suspend fun getUserSubscriptions(@Query("subscription_type") subscriptionType: String): UserSubscriptionResponse

    @GET("users/transactions")
    suspend fun getUserTransactions(): TransactionResponse

    @GET("users/sessions")
    suspend fun getUserSessions(): SessionsResponse

    @GET("users/completed-courses")
    suspend fun getCompletedCourses(@Query("_lang") lang: String): CompletedCourseResponse

    @Multipart
    @POST("users/image")
    suspend fun changeImage(
        @Part image: MultipartBody.Part,
        @Part("_method") method: RequestBody
    ): ChangeImageResponse

    @PATCH("users/profile")
    suspend fun changeUsername(@Body updateUser: UpdateUsernameRequest,@Query("_lang") lang: String): UserInfoResponse

    @PATCH("users/password")
    suspend fun changePassword(@Body updatePassword: ChangePasswordRequest): ChangePasswordResponse

    @GET("videos/{id}")
    suspend fun getVideoInfo(@Path("id") videoId: Long,@Query("_lang") lang: String): VideoInfoResponse

    @GET("videos/{id}/references")
    suspend fun getVideoFiles(@Path("id") videoId: Long): VideoFilesResponse

    @POST("videos/{id}/watched")
    suspend fun addVideoToHistory(@Path("id") videoId: Long): WatchHistoryResponse

    @POST("analytics/watch-time")
    suspend fun watchTime(@Body watchTime: WatchTimeRequest): WatchTimeResponse

    @POST("/comments")
    suspend fun createComment(@Body createCommentRequest: CreateCommentRequest): CreateCommentResponse

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: Long): WatchHistoryResponse

    @POST("comments/{id}")
    suspend fun editComment(
        @Path("id") commentId: Long,
        @Body editCommentRequest: EditCommentRequest
    ): EditCommentResponse

    @POST("subscriptions/purchase")
    suspend fun purchaseSubscription(@Body purchaseRequest: PurchaseRequest): PurchaseResponse

    @GET("subscription/details")
    suspend fun subscriptionDetails(
        @Query("subscription_type") subscriptionType: String,
        @Query("resource_id") resourceId: Long,
        @Query("_lang") lang: String
    ): SubscriptionDetailsResponse

    @GET("subscriptions")
    suspend fun subscriptionsTable(): SubscriptionsTable

    @GET("/configuration/{type}")
    suspend fun checkoutBalance(@Path("type") checkoutType: String,@Query("_lang") lang: String): CheckoutResponse
}