package com.plcoding.androidstorage.microphone

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaRecorder
import android.widget.TextView
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.plcoding.androidstorage.microphone.MicActivity
import android.content.pm.PackageManager
import android.content.ContextWrapper
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.view.View
import android.widget.ImageView
import com.plcoding.androidstorage.FirebaseUpload
import com.plcoding.androidstorage.R
import com.plcoding.androidstorage.databinding.ActivityMainBinding
import com.plcoding.androidstorage.databinding.ActivityMicBinding
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

class MicActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var ibRecord: ImageView? = null
    private var ibPlay: ImageView? = null
    private var tvTime: TextView? = null
    private var tvRecordingPath: TextView? = null
    private var ivBg: ImageView? = null
    var isRecording = false
    var isPlaying = false
    private var currentTime = Calendar.getInstance().time
    var seconds = 0
    private var path: String? = null
    var dummySeconds = 0
    var playableSeconds = 0
    var handler: Handler? = null
    private var executorService = Executors.newSingleThreadExecutor()
    private lateinit var binding: ActivityMicBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        ibRecord = findViewById(R.id.ib_record)
        ibPlay = findViewById(R.id.ib_play)
        tvTime = findViewById(R.id.tv_time)
        tvRecordingPath = findViewById(R.id.tv_recording_path)
        ivBg = findViewById(R.id.iv_bg)
        mediaPlayer = MediaPlayer()
        binding.ibRecord.setOnClickListener(View.OnClickListener {
            if (checkRecordingPermission()) {
                if (!isRecording) {
                    isRecording = true
                    executorService.execute {
                        mediaRecorder = MediaRecorder()
                        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        mediaRecorder!!.setOutputFile(recordingFile.path)
                        path = recordingFile.path
                        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        try {
                            mediaRecorder!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        mediaRecorder!!.start()
                        runOnUiThread {
                            binding.ivBg.setVisibility(View.VISIBLE)
                            binding.tvRecordingPath.setText(recordingFile.path)
                            playableSeconds = 0
                            seconds = 0
                            dummySeconds = 0
                            binding.ibRecord.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@MicActivity,
                                    R.drawable.recording
                                )
                            )
                            runTimer()
                        }
                    }
                } else {
                    executorService.execute {
                        mediaRecorder!!.stop()
                        mediaRecorder!!.release()
                        FirebaseUpload.uploadAudio(Uri.fromFile(recordingFile))
                        mediaRecorder = null
                        playableSeconds = seconds
                        dummySeconds = seconds
                        seconds = 0
                        isRecording = false
                        runOnUiThread {
                            binding.ivBg.setVisibility(View.VISIBLE)
                            handler!!.removeCallbacksAndMessages(null)
                            binding.ibRecord.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@MicActivity,
                                    R.drawable.not_recording
                                )
                            )
                        }
                    }
                }
            } else {
                requestRecordingPermission()
            }
        })
        binding.ibPlay.setOnClickListener(View.OnClickListener {
            if (!isPlaying) {
                if (path != null) {
                    try {
                        mediaPlayer!!.setDataSource(recordingFile.path)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(applicationContext, "No Recording Present", Toast.LENGTH_SHORT)
                        .show()
                    return@OnClickListener
                }
                try {
                    mediaPlayer!!.prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                mediaPlayer!!.start()
                isPlaying = true
                binding.ibPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MicActivity,
                        R.drawable.pause
                    )
                )
                binding.ivBg.setVisibility(View.GONE)
                runTimer()
            } else {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
                mediaPlayer = MediaPlayer()
                isPlaying = false
                seconds = 0
                handler!!.removeCallbacksAndMessages(null)
                binding.ivBg.setVisibility(View.VISIBLE)
                binding.ibPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MicActivity,
                        R.drawable.play
                    )
                )
            }
        })
    }

    private fun runTimer() {
        handler = Handler()
        handler!!.post(object : Runnable {
            override fun run() {
                val minutes = seconds % 3600 / 60
                val secs = seconds % 60
                val time = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
                tvTime!!.text = time
                if (isRecording || isPlaying && playableSeconds != -1) {
                    seconds++
                    playableSeconds++
                    if (playableSeconds == -1 && isPlaying) {
                        mediaPlayer!!.stop()
                        mediaPlayer!!.release()
                        isPlaying = false
                        mediaPlayer = null
                        mediaPlayer = MediaPlayer()
                        playableSeconds = dummySeconds
                        seconds = 0
                        handler!!.removeCallbacksAndMessages(null)
                        ivBg!!.visibility = View.VISIBLE
                        ibPlay!!.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@MicActivity,
                                R.drawable.play
                            )
                        )
                        return
                    }
                }
                handler!!.postDelayed(this, 1000)
            }
        })
    }

    private fun requestRecordingPermission() {
        ActivityCompat.requestPermissions(
            this@MicActivity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_AUDIO_PERMISSION_CODE
        )
    }

    fun checkRecordingPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_DENIED
        ) {
            requestRecordingPermission()
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord) {
                    Toast.makeText(applicationContext, "Permission Given", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private val recordingFile: File
        private get() {
            val contextWrapper = ContextWrapper(applicationContext)
            val music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(music, "TempRecording" + ".mp3")
            return file
        }

    companion object {
        private const val REQUEST_AUDIO_PERMISSION_CODE = 101
    }
}