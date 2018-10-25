package com.baikaleg.v3.autoinfo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.ANNOUNCE_AUDIO_TYPE_PLAYER
import com.baikaleg.v3.autoinfo.data.ANNOUNCE_AUDIO_TYPE_TTS
import com.baikaleg.v3.autoinfo.data.QueryPreferences
import java.util.*

const val VOICE_PATH = "/AutoInfo/voice/"

class AudioController(private val context: Context) :
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener,
        TextToSpeech.OnInitListener,
        UtteranceProgressListener() {

    private var station: String? = null
    private var player: MediaPlayer = MediaPlayer().apply {
        setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
    }
    private lateinit var playbackAttributes: AudioAttributes
    private lateinit var focusRequest: AudioFocusRequest

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private val pref = QueryPreferences(context)
    private val am: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
        }

        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        telephony.listen(StatePhoneReceiver(), PhoneStateListener.LISTEN_CALL_STATE)
    }

    /**
     * @param stg- short description of station
     * @param type - <code>0</code> empty prefix
     *               <code>1</code> current station prefix
     *               <code>2</code> next station prefix
     */
    fun announceStation(stg: String?, type: Int) {
        station = stg
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Thread.sleep(1000)
            when (pref.getAnnounceAudioType()) {
                ANNOUNCE_AUDIO_TYPE_TTS -> startTTS(type)
                ANNOUNCE_AUDIO_TYPE_PLAYER -> startPlayer(type)
            }
        }
    }

    fun setVolumeLevel(index: Int) {
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, index, 0)
    }

    fun cancel() {
        player.release()
        tts.stop()
        tts.shutdown()
    }

    private fun startTTS(type: Int) {
        var text: String? = null
        when (type) {
            0 -> text = station
            1 -> text = context.getString(R.string.station_tts) + station
            2 -> text = context.getString(R.string.next_station_tts) + station
        }
        tts.setOnUtteranceProgressListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts")
        } else {
            @Suppress("DEPRECATION")
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    private fun startPlayer(type: Int) {
        try {
            when (type) {
                0 -> {
                    val uri = Environment.getExternalStorageDirectory().path + VOICE_PATH + station + ".3gp"
                    player = MediaPlayer.create(context, Uri.parse(uri))
                    station = null
                }
                1 -> player = MediaPlayer.create(context, R.raw.station_voice)
                2 -> player = MediaPlayer.create(context, R.raw.station_next_voice)
            }
            player.setOnCompletionListener(this)
            player.start()
        } catch (e: Exception) {
            Log.e("startPlayerError", e.message)
        }
    }

    private fun abandonAudioFocus() {
        Thread.sleep(1000)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(this)
        } else {
            am.abandonAudioFocusRequest(focusRequest)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (station == null) {
            mp?.release()
            abandonAudioFocus()
        } else {
            startPlayer(0)
        }
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("ttsInitError", "This language is not supported")
            }
        } else {
            Log.e("ttsInitError", "Initialization failed")
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onError(utteranceId: String?) {
    }

    override fun onStart(utteranceId: String?) {
    }

    override fun onDone(utteranceId: String?) {
        abandonAudioFocus()
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