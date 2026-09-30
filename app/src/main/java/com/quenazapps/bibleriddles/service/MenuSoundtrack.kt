package com.quenazapps.bibleriddles.service

import android.app.Activity
import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.quenazapps.bibleriddles.MainMenuActivity
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.StagesListActivity
import com.quenazapps.bibleriddles.activity.TutorialActivity

/** One player survives menu-to-menu navigation; gameplay never owns this soundtrack. */
internal class MenuSoundtrack(private val context: Context) : Application.ActivityLifecycleCallbacks {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val startedMenus = mutableSetOf<Activity>()
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    internal var player: MediaPlayer? = null
        private set
    private var prepared = false
    private var menuForeground = false
    private var focusRequested = false
    private var hasFocus = false

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (focusRequested) {
            hasFocus = change == AudioManager.AUDIOFOCUS_GAIN
            if (hasFocus && menuForeground) {
                if (prepared) player?.start()
            } else if (change == AudioManager.AUDIOFOCUS_LOSS) {
                release()
            } else if (prepared) {
                player?.pause()
            }
        }
    }
    private val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(focusListener, Handler(Looper.getMainLooper()))
            .build()
    } else null

    private fun Activity.isMenu() = this is MainMenuActivity ||
        this is StagesListActivity || this is TutorialActivity

    override fun onActivityStarted(activity: Activity) {
        if (activity.isMenu()) {
            startedMenus.add(activity)
        } else {
            // Includes the stage-title transition and all stage/tip activities.
            menuForeground = false
            release()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        menuForeground = activity.isMenu()
        if (menuForeground) play() else release()
    }

    override fun onActivityStopped(activity: Activity) {
        startedMenus.remove(activity)
        // The destination menu starts before its predecessor stops, so the player stays intact.
        if (startedMenus.isEmpty() && !activity.isChangingConfigurations) {
            menuForeground = false
            release()
        }
    }

    @Suppress("DEPRECATION")
    private fun play() {
        if (!focusRequested) {
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.requestAudioFocus(requireNotNull(focusRequest))
            } else {
                audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }
            hasFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            focusRequested = hasFocus
        }
        if (!hasFocus) return
        player?.let {
            if (prepared && !it.isPlaying) it.start()
            return
        }
        try {
            val newPlayer = MediaPlayer()
            player = newPlayer
            newPlayer.setAudioAttributes(attributes)
            context.resources.openRawResourceFd(R.raw.main_soundtrack).use { file ->
                newPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.length)
            }
            newPlayer.isLooping = true
            newPlayer.setOnPreparedListener { ready ->
                if (player === ready) {
                    prepared = true
                    if (menuForeground && hasFocus) ready.start()
                }
            }
            newPlayer.setOnErrorListener { failed, what, extra ->
                Log.w("MenuSoundtrack", "Playback error: $what/$extra")
                if (player === failed) release()
                true
            }
            // Decode off the UI thread so opening the menu remains responsive.
            newPlayer.prepareAsync()
        } catch (error: Exception) {
            Log.w("MenuSoundtrack", "Unable to load menu soundtrack", error)
            release()
        }
    }

    @Suppress("DEPRECATION")
    private fun release() {
        player?.release()
        player = null
        prepared = false
        hasFocus = false
        if (focusRequested) {
            focusRequested = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(requireNotNull(focusRequest))
            } else {
                audioManager.abandonAudioFocus(focusListener)
            }
        }
    }

    // Pausing happens during every Activity handoff; stopping here would interrupt menu navigation.
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) { startedMenus.remove(activity) }
}
