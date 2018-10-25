package com.baikaleg.v3.autoinfo.ui.stations.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.view.MotionEvent
import android.widget.ImageButton
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.audio.VOICE_PATH


open class RecordStationVoiceDialog : DialogFragment() {
    private lateinit var vibe: Vibrator
    private lateinit var output: String
    private var recorder: MediaRecorder? = MediaRecorder()

    companion object {
        private val TAG = RecordStationVoiceDialog::class.qualifiedName

        val ARG_DESC = "desc"
        fun show(activity: Activity, desc: String) {

            RecordStationVoiceDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_DESC, desc)
                }
            }.show(activity.fragmentManager, TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibe = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val description = arguments.getString(ARG_DESC)
        output = Environment.getExternalStorageDirectory().path + VOICE_PATH + description + ".3gp"

        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        recorder?.setOutputFile(output)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = activity.layoutInflater.inflate(R.layout.dialog_record_station_voice, null)
        val btn = view.findViewById<ImageButton>(R.id.start_stop_recording_btn)
        btn.setOnTouchListener({ v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startRecording(btn)
            }
            if (event.action == MotionEvent.ACTION_UP) {
                stopRecording(btn)
            }
            true
        })
        return AlertDialog.Builder(activity)
                .setView(view)
                .setTitle("Нажмите для записи голоса")
                .setNegativeButton(activity.getString(android.R.string.cancel)){ _, _ -> dismiss() }
                .create()
                .apply { setCanceledOnTouchOutside(false) }
    }

    private fun startRecording(btn: ImageButton) {
        btn.setBackgroundResource(R.drawable.ic_record_stop)
        try {
            vibe.vibrate(100)
            recorder?.prepare()
            recorder?.start()
        } catch (e: Exception) {

        }
    }

    private fun stopRecording(btn: ImageButton) {
        btn.setBackgroundResource(R.drawable.ic_record_start)
        vibe.vibrate(100)
        recorder?.stop()
        recorder?.release()
        recorder = null
        dismiss()
    }
}


