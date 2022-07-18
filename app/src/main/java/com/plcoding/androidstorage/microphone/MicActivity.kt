package com.plcoding.androidstorage

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MicActivity : AppCompatActivity() {
    private lateinit var mr : MediaRecorder
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    @RequiresApi(Build.VERSION_CODES.S)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mic)

         button1 = findViewById(R.id.button1)
         button2 = findViewById(R.id.button2)
         button3  = findViewById(R.id.button3)

        val path = Environment.getExternalStorageDirectory().toString()+"/myRecording.3gp"
        mr = MediaRecorder()
        button1.isEnabled = false
        button2.isEnabled = false

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO,
                                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE),111)
            button1.isEnabled = true

            //Start Recording
            button1.setOnClickListener {
                mr.setAudioSource(MediaRecorder.AudioSource.MIC)
                mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mr.setOutputFile(path)
                mr.prepare()
                mr.stop()
                button2.isEnabled = true
                button1.isEnabled = false

            }

            //Stop Recording
            button2.setOnClickListener {
                mr.stop()
                button1.isEnabled = true
                button2.isEnabled = false
            }

            //Play Recording
            button3.setOnClickListener {
                val mp = MediaPlayer()
                mp.setDataSource(path)
                mp.prepare()
                mp.start()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==111 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            button1.isEnabled = true
    }
}