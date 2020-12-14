package uz.napa.foxmedia.util

import android.view.Window
import android.view.WindowManager
import com.longtailvideo.jwplayer.JWPlayerView
import com.longtailvideo.jwplayer.events.*
import com.longtailvideo.jwplayer.events.listeners.AdvertisingEvents.*
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents.*

/**
 * Sets the FLAG_KEEP_SCREEN_ON flag during playback - disables it when playback is stopped
 */
class KeepScreenOnHandler(jwPlayerView: JWPlayerView, window: Window) :
    OnPlayListener, OnPauseListener, OnCompleteListener, OnErrorListener,
    OnAdPlayListener, OnAdPauseListener, OnAdCompleteListener, OnAdSkippedListener,
    OnAdErrorListener {
    /**
     * The application window
     */
    private val mWindow: Window
    private fun updateWakeLock(enable: Boolean) {
        if (enable) {
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onError(errorEvent: ErrorEvent) {
        updateWakeLock(false)
    }

    override fun onAdPlay(adPlayEvent: AdPlayEvent) {
        updateWakeLock(true)
    }

    override fun onAdPause(adPauseEvent: AdPauseEvent) {
        updateWakeLock(false)
    }

    override fun onAdComplete(adCompleteEvent: AdCompleteEvent) {
        updateWakeLock(false)
    }

    override fun onAdSkipped(adSkippedEvent: AdSkippedEvent) {
        updateWakeLock(false)
    }

    override fun onAdError(adErrorEvent: AdErrorEvent) {
        updateWakeLock(false)
    }

    override fun onComplete(completeEvent: CompleteEvent) {
        updateWakeLock(false)
    }

    override fun onPause(pauseEvent: PauseEvent) {
        updateWakeLock(false)
    }

    override fun onPlay(playEvent: PlayEvent) {
        updateWakeLock(true)
    }

    init {
        jwPlayerView.addOnPlayListener(this)
        jwPlayerView.addOnPauseListener(this)
        jwPlayerView.addOnCompleteListener(this)
        jwPlayerView.addOnErrorListener(this)
        jwPlayerView.addOnAdPlayListener(this)
        jwPlayerView.addOnAdPauseListener(this)
        jwPlayerView.addOnAdCompleteListener(this)
        jwPlayerView.addOnAdErrorListener(this)
        mWindow = window
    }
}