package com.baikaleg.v3.autoinfo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Station

class AudioController(private val context: Context) : MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private var station: Station? = null
    private var player: MediaPlayer = MediaPlayer().apply {
        setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
    }
    private val am: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private lateinit var playbackAttributes: AudioAttributes
    private lateinit var focusRequest: AudioFocusRequest

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
        }
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(StatePhoneReceiver(), PhoneStateListener.LISTEN_CALL_STATE)
    }

    fun announceCurrentStation(station: Station?, type: Int) {
        this.station = station
        when (type) {
            0 -> player = MediaPlayer.create(context, R.raw.station_voice)
            1 -> player = MediaPlayer.create(context, R.raw.station_next_voice)
        }

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              am.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.setOnCompletionListener(this)
            player.start()
        }
    }

    fun setVolumeLevel(index: Int) {
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, index, 0)
    }

    fun onDestroy() {
        player.release()
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(this)
        } else {
             am.abandonAudioFocusRequest(focusRequest)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.release()
        player = MediaPlayer.create(context, R.raw.test_voice)
        player.start()
        abandonAudioFocus()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {

            }
            AudioManager.AUDIOFOCUS_GAIN -> {

            }
            AudioManager.AUDIOFOCUS_LOSS -> {

            }
        }
    }

    private fun forceSpeakerOutput(on: Boolean) {
        if (on) {
            am.mode = AudioManager.MODE_IN_CALL
            am.isSpeakerphoneOn = true
        } else {
            am.mode = AudioManager.MODE_NORMAL
            am.isSpeakerphoneOn = false
        }
    }

    inner class StatePhoneReceiver : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING, TelephonyManager.CALL_STATE_OFFHOOK -> {
                    forceSpeakerOutput(true)
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    forceSpeakerOutput(false)
                }
            }
        }
    }
}