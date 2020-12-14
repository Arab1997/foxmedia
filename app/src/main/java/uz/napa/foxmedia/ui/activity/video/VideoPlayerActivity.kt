package uz.napa.foxmedia.ui.activity.video

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.AbsListView
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem
import kotlinx.android.synthetic.main.activity_video.*
import kotlinx.android.synthetic.main.bottom_sheet_comment.*
import kotlinx.android.synthetic.main.bottom_sheet_comment.bottom_sheet_comment
import kotlinx.android.synthetic.main.bottom_sheet_comment.comments_progress
import kotlinx.android.synthetic.main.bottom_sheet_files.*
import kotlinx.android.synthetic.main.bottom_sheet_teacher.*
import kotlinx.android.synthetic.main.bottom_sheet_teacher.btn_close_sheet
import kotlinx.android.synthetic.main.video_controller.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.analytics.WatchTimeRequest
import uz.napa.foxmedia.request.comment.CreateCommentRequest
import uz.napa.foxmedia.request.comment.EditCommentRequest
import uz.napa.foxmedia.response.course.Instructor
import uz.napa.foxmedia.response.video.Video
import uz.napa.foxmedia.response.video.comment.Comment
import uz.napa.foxmedia.ui.activity.video.`interface`.SeekBarListener
import uz.napa.foxmedia.ui.activity.video.adapter.CommentsAdapter
import uz.napa.foxmedia.ui.activity.video.adapter.FilesAdapter
import uz.napa.foxmedia.ui.activity.video.adapter.VideoAdapter
import uz.napa.foxmedia.util.*
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

const val COURSE_ID = "course_id"
const val INSTRUCTOR = "INSTRUCTOR"

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var myPref: MyPreference
    private lateinit var exoPlayer: SimpleExoPlayer
    private val videoAdapter by lazy { VideoAdapter() }
    private val commentsAdapter by lazy { CommentsAdapter() }
    private val filesAdapter by lazy { FilesAdapter() }
    private var currentVideoId = -1L
    private val videoVM by viewModels<VideoViewModel> {
        getViewModelFactory(
            MyRepository(),
            intent.getLongExtra(COURSE_ID, -1)
        )
    }
    private val mHandler = Handler(Looper.getMainLooper())
    private var runnable: Runnable = object : Runnable {
        override fun run() {
            val bufferedPos = exoPlayer.bufferedPosition
            video_progress.secondaryProgress = bufferedPos.toInt()
            val currentPos = exoPlayer.currentPosition
            if (exoPlayer.isPlaying) {
                videoVM.watchTimeRequest(
                    WatchTimeRequest(
                        videoId,
                        floor((currentPos.toDouble() / 1000)),
                        floor(exoPlayer.duration.toDouble() / 1000),
                        1
                    )
                )
            }

            video_progress.progress = currentPos.toInt()
            tv_current_position.text =
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPos)
            mHandler.postDelayed(this, 1_000)
        }
    }

    private var isPortrait = false
    private var videoId = -1L
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
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                videoVM.getComments(currentVideoId)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        setContentView(R.layout.activity_video)
        setUpRv()
        setUpTeacher()
        setUpComment()
        setUpFiles()
//        setUpJwPlayer()
        setUpExoPlayer()
        setUpListeners()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        videoVM.courseVideos.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    startLoading()
                }
                is Resource.Error -> {
                    hideLoading()
                    snackbar(activity_jwplayerview, it.message.toString())
                }
                is Resource.Success -> {
                    hideLoading()
                    it.data?.let { response ->
                        val list = response.videos.data
                        videoAdapter.differ.submitList(list)
                        if (list.isNotEmpty()) {
                            val vid = list[0]
                            tv_video_name.text = vid.name
                            video_rating.rating = getRating(vid.rating)
                            videoVM.getVideoInfo(vid.id)
                            videoVM.getComments(vid.id)
                            currentVideoId = vid.id
                        }
                    }
                }
            }
        })

        videoVM.videoInfo.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    startLoading()
                }
                is Resource.Error -> {
                    hideLoading()
                    snackbar(activity_jwplayerview, it.message.toString())
                }
                is Resource.Success -> {
                    hideLoading()
                    it.data?.let { response ->
                        response.video?.let { video ->
                            setUpVideo(video)
                            videoVM.getComments(video.id)
                            currentVideoId = video.id
                            videoVM.getFiles(video.id)
                        } ?: kotlin.run {
                            btn_buy.isVisible = true
                            btn_buy.setOnClickListener {
                                onBackPressed()
                            }
                            tv_video_name.text = response.message
                        }
                    }
                }
            }
        })

        videoVM.comments.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    startCommentsLoading()
                }
                is Resource.Error -> {
                    hideCommentsLoading()
                    snackbar(activity_jwplayerview, it.message.toString())
                }
                is Resource.Success -> {
                    hideCommentsLoading()
                    it.data?.let { response ->
                        commentsAdapter.differ.submitList(response.comments.data.reversed())
                        val totalPages = response.comments.total / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = videoVM.commentsPage == totalPages
                    }
                }
            }
        })

        videoVM.createdComment.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    showLoadingSendComment()
                }
                is Resource.Error -> {
                    hideLoadingSendComment()
                    snackbar(activity_jwplayerview, it.message.toString())
                }
                is Resource.Success -> {
                    hideLoadingSendComment()
                    it.data?.let { response ->
                        val list = ArrayList<Comment>()
                        list.addAll(commentsAdapter.differ.currentList)
                        list.add(0, response.comment)
                        commentsAdapter.submitList(list)
                        et_comment.text = null
                        video_rate.rating = 0f
                    }
                }
            }
        })

        videoVM.files.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    snackbar(activity_jwplayerview, it.message.toString())
                }
                is Resource.Success -> {
                    it.data?.let { response ->
                        filesAdapter.differ.submitList(response.references)
                    }
                }
            }
        })
    }

    private fun setUpExoPlayer() {
        exoPlayer = SimpleExoPlayer.Builder(this).build()
        exo_player.player = exoPlayer
        exoPlayer.playWhenReady = true
        setUpOrientationButton()
        setUpPlayerControllers()
        setUpSeekBar()

    }

    private fun updateSeekbar() {
        video_progress.max = exoPlayer.duration.toInt()
        tv_current_position.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(exoPlayer.duration)
        runOnUiThread(runnable)
    }

    private fun setUpPlayerControllers() {
        btn_play.setOnClickListener {
            if (exoPlayer.isPlaying) {
                btn_play.setImageResource(R.drawable.pause_to_play)
                val draw = btn_play.drawable
                if (draw is AnimatedVectorDrawable) {
                    draw.start()
                    exoPlayer.playWhenReady = false
                } else if (draw is AnimatedVectorDrawableCompat) {
                    draw.start()
                    exoPlayer.playWhenReady = false
                }
            } else {
                btn_play.setImageResource(R.drawable.play_to_pause)
                val draw = btn_play.drawable
                if (draw is AnimatedVectorDrawable) {
                    draw.start()
                    exoPlayer.playWhenReady = true
                } else if (draw is AnimatedVectorDrawableCompat) {
                    draw.start()
                    exoPlayer.playWhenReady = true
                }
            }
        }

        btn_frw.setOnClickListener {
            val rotate = RotateAnimation(
                0f, 90f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotate.apply {
                duration = 200
                interpolator = LinearInterpolator()
            }
            btn_frw.startAnimation(rotate)
            exoPlayer.seekTo(exoPlayer.currentPosition + 10_000)
        }

        btn_rewind.setOnClickListener {
            val rotate = RotateAnimation(
                0f, -90f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotate.apply {
                duration = 200
                interpolator = LinearInterpolator()
            }
            btn_rewind.startAnimation(rotate)
            exoPlayer.seekTo(exoPlayer.currentPosition - 10_000)
        }
    }

    private fun setUpSeekBar() {
        video_progress.setOnSeekBarChangeListener(object : SeekBarListener() {
            var userSelectedPosition = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    userSelectedPosition = progress
                }
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                exoPlayer.seekTo(userSelectedPosition.toLong())
            }
        })
    }

    private fun setUpOrientationButton() {
        isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        if (isPortrait)
            btn_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_enter)
        else
            btn_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_exit)

        btn_full_screen.setOnClickListener {
            isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            if (isPortrait) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                mHandler.postDelayed({
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }, 3000)
                btn_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_exit)
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mHandler.postDelayed({
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }, 3000)
                btn_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_enter)
            }
        }
    }


    private fun hideLoadingSendComment() {
        send_comment_progress.isVisible = false
        btn_send_comment.isVisible = true
        et_comment.isEnabled = true
    }

    private fun showLoadingSendComment() {
        send_comment_progress.isVisible = true
        btn_update_comment.isVisible = false
        btn_edit_comment.isVisible = false
        btn_delete_comment.isVisible = false
        btn_send_comment.isVisible = false
        et_comment.isEnabled = false
    }

    private fun setUpVideo(video: Video) {
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when (state) {
                    Player.STATE_BUFFERING -> {
                        progress_video.isVisible = true
                    }
                    Player.STATE_READY -> {
                        progress_video.isVisible = false
                        tv_duration.text = SimpleDateFormat(
                            "/mm:ss",
                            Locale.getDefault()
                        ).format(exoPlayer.duration)
                        updateSeekbar()
                        videoVM.addVideoToHistory(video.id)
                    }
                }
            }
        })

        btn_buy.isVisible = false
        tv_description.text = htmlFormat(video.description)
        tv_publish_time.text =
            getString(R.string.published, formatDate(video.createdAt))
        val mediaItem =
            MediaItem.fromUri(Uri.parse("https://cdn.jwplayer.com/manifests/${video.mediaId}.m3u8"))
//        val playerItem = PlaylistItem.Builder()
//            .file("https://cdn.jwplayer.com/manifests/${video.mediaId}.m3u8")
//            .image(BASE_URL + video.image)
//            .title(video.name)
//            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exo_player.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        exoPlayer.play()
//        jwPlayer.load(playerItem)


        var mOldTime = 0.0

    }

    private fun setUpTeacher() {
        val instructor = intent.getParcelableExtra<Instructor>(INSTRUCTOR)
        val sheetBehavior = BottomSheetBehavior.from(bottom_sheet_teacher)

        instructor?.let {
            tv_teacher_name.text =
                getString(R.string.full_name, instructor.lastName, instructor.firstName)
            tv_teacher_info.text = htmlFormat(instructor.briefIntroduction)
            Glide.with(this).load(BASE_URL + instructor.image)
                .placeholder(R.drawable.avatar_placeholder)
                .into(img_teacher)
            btn_close_sheet.setOnClickListener {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            tv_teacher.setOnClickListener {
                if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                    sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        } ?: kotlin.run {
            tv_teacher.setOnClickListener {
                snackbar(activity_jwplayerview, getString(R.string.no_teacher_info))
            }
        }
    }

    private fun setUpComment() {
        var currentComment: Comment? = null
        var currentPosition: Int? = null
        videoVM.deleteComment.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    showLoadingSendComment()
                }
                is Resource.Error -> {
                    hideLoadingSendComment()
                    dismissSelection()
                    snackbar(activity_jwplayerview, it.message.toString())
                }
                is Resource.Success -> {
                    hideLoadingSendComment()
                    it.data?.let { response ->
                        val list = ArrayList<Comment>()
                        list.addAll(commentsAdapter.differ.currentList)
                        dismissSelection()
                        currentComment?.isChecked = false
                        list.remove(currentComment)
                        commentsAdapter.submitList(list)
                        snackbar(bottom_sheet_comment, response.message)
                    }
                }
            }
        })

        videoVM.editComment.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    showLoadingSendComment()
                }
                is Resource.Error -> {
                    hideLoadingSendComment()
                    dismissSelection()
                    snackbar(activity_jwplayerview, it.message.toString())
                }
                is Resource.Success -> {
                    hideLoadingSendComment()
                    it.data?.let { response ->
                        val list = ArrayList<Comment>()
                        list.addAll(commentsAdapter.differ.currentList)
                        commentsAdapter.selectedVideo = -1
                        currentPosition?.let { pos ->
                            list.removeAt(pos)
                            list.add(pos, response.comment)
                            commentsAdapter.submitList(list)
                        }
                        dismissSelection()
                    }
                }
            }
        })
        commentsAdapter.setOnClickListener { comment, position ->
            if (comment.isChecked) {
                currentComment = comment
                currentPosition = position
                selectComment()
                btn_delete_comment.setOnClickListener {
                    videoVM.deleteComment(comment.id)
                }
                btn_edit_comment.setOnClickListener {
                    btnEditChoosed()
                    et_comment.setText(comment.body)
                }
                et_comment.addTextChangedListener {
                    btn_update_comment.isEnabled = et_comment.text.toString().isNotEmpty()
                }
                btn_update_comment.setOnClickListener {
                    videoVM.editComment(
                        comment.id,
                        EditCommentRequest(et_comment.text.toString())
                    )
                }
            } else
                dismissSelection()
        }
        btn_send_comment.isEnabled =
            et_comment.text.toString().isNotEmpty() and (video_rate.rating > 0f)
        val sheetBehavior = BottomSheetBehavior.from(bottom_sheet_comment)
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheet.hideKeyboard()
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    videoVM.getComments(currentVideoId)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })
        rv_comments.setOnTouchListener { view, motionEvent ->
            sheetBehavior.isDraggable = motionEvent.action == MotionEvent.ACTION_UP
            view.performClick()
            return@setOnTouchListener false
        }
        btn_send_comment.setOnClickListener {
            currentComment?.let {
                it.isChecked = false
            }
            dismissSelection()
            videoVM.createComment(
                CreateCommentRequest(
                    currentVideoId.toString(),
                    (video_rate.rating * 20).toInt(),
                    et_comment.text.toString()
                )
            )
        }

        tv_comment.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        btn_close_comments.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        et_comment.addTextChangedListener {
            it?.let { editable ->
                btn_send_comment.isEnabled =
                    et_comment.text.toString().isNotEmpty() and (video_rate.rating > 0f)
            }
        }
        video_rate.setOnRatingBarChangeListener { ratingBar, fl, b ->
            btn_send_comment.isEnabled =
                et_comment.text.toString().isNotEmpty() and (video_rate.rating > 0f)
        }

    }

    private fun setUpFiles() {

        val sheetBehavior = BottomSheetBehavior.from(bottom_sheet_files)

        tv_file.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        rv_files.setOnTouchListener { view, motionEvent ->
            sheetBehavior.isDraggable = motionEvent.action == MotionEvent.ACTION_UP
            view.performClick()
            return@setOnTouchListener false
        }

        filesAdapter.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(BASE_URL + it.filename)
            startActivity(intent)
        }

        btn_close_files.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

    }

    private fun btnEditChoosed() {
        btn_send_comment.isVisible = false
        btn_update_comment.isVisible = true
        video_rate.isVisible = false
    }

    private fun selectComment() {
        btn_close_comments.isVisible = false
        btn_delete_comment.isVisible = true
        btn_edit_comment.isVisible = true
    }

    private fun dismissSelection() {
        commentsAdapter.selectedVideo = -1
        et_comment.text = null
        btn_send_comment.isVisible = true
        video_rate.isVisible = true
        btn_close_comments.isVisible = true
        btn_update_comment.isVisible = false
        btn_delete_comment.isVisible = false
        btn_edit_comment.isVisible = false
    }

    private fun hideCommentsLoading() {
        comments_progress.isVisible = false
    }

    private fun startCommentsLoading() {
        comments_progress.isVisible = true
    }

    private fun setUpListeners() {
        tv_video_name.setOnClickListener {
            hideShowDescription()
        }

        videoAdapter.setOnClickListener { videoInfo ->
            tv_video_name.text = videoInfo.name
            videoVM.getVideoInfo(videoInfo.id)
        }
    }

    private fun hideShowDescription() {
        if (widget_video_info.isVisible) {
            widget_video_info.isVisible = false
            tv_video_name.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_drop_down,
                0
            )
        } else {
            widget_video_info.isVisible = true
            tv_video_name.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_drop_up,
                0
            )
        }
        TransitionManager.beginDelayedTransition(activity_jwplayerview, AutoTransition())
    }

    private fun setUpJwPlayer() {
//        jwPlayer = findViewById(R.id.exo_player)
//        KeepScreenOnHandler(jwPlayer, window)
//        jwPlayer.addOnCompleteListener { }
    }

    private fun setUpRv() {
        rv_video.apply {
            adapter = videoAdapter
            itemAnimator = DefaultItemAnimator()
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this.context, R.anim.layout_animation_fall_down)
        }

        rv_comments.apply {
            adapter = commentsAdapter
            itemAnimator = DefaultItemAnimator()
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this.context, R.anim.layout_animation_fall_down)
            addOnScrollListener(rvOnScrollListener)
        }

        rv_files.apply {
            adapter = filesAdapter
            itemAnimator = DefaultItemAnimator()
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this.context, R.anim.layout_animation_fall_down)
        }
    }

    private fun startLoading() {
        progress_video.isVisible = true
    }

    private fun hideLoading() {
        progress_video.isVisible = false
    }

    override fun onStop() {
        btn_play.setImageResource(R.drawable.play_to_pause)
        exoPlayer.pause()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(runnable)
        exoPlayer.release()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        // Set fullscreen when the device is rotated to landscape
//        jwPlayer.setFullscreen(
//            newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE,
//            true
//        )
        when (val orientation = newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> btn_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_enter)
            Configuration.ORIENTATION_LANDSCAPE -> btn_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_exit)
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mHandler.postDelayed({
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }, 3000)
                btn_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_enter)
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun attachBaseContext(newBase: Context?) {
        myPref = MyPreference(newBase!!)
        val lang = myPref.getLang()
        super.attachBaseContext(MyContextWrapper.wrap(newBase, lang))
    }


}