package com.plcoding.androidstorage

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.os.Bundle
import com.plcoding.androidstorage.databinding.ActivityCameraBinding
import com.plcoding.androidstorage.databinding.ActivityMainBinding
import com.plcoding.androidstorage.storage.StorageActivity

class MainActivity: AppCompatActivity()  {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.camera.setOnClickListener{
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        binding.storage.setOnClickListener{
            val intent = Intent(this, StorageActivity::class.java)
            startActivity(intent)
        }

    }


}