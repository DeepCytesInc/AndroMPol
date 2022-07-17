package com.plcoding.androidstorage.storage

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.ActivityResultCallback
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.Toast
import com.plcoding.androidstorage.FirebaseUpload
import com.plcoding.androidstorage.databinding.ActivityReadExternalStorageBinding

class ReadExternalStorage : AppCompatActivity() {
    //Once view binding is enabled in a module, it generates a binding class for each XML layout file present in that module
    private var binding: ActivityReadExternalStorageBinding? = null
    private var nTakePhoto: ActivityResultLauncher<String>? = null
    private val selectImagebtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadExternalStorageBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        init()
    }

    private fun init() {
        nTakePhoto = registerForActivityResult(
            GetContent()
        ) { result -> binding!!.firebaseimage.setImageURI(result)
            FirebaseUpload.uploadPicture(result)
        }
        binding!!.selectImagebtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@ReadExternalStorage,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                nTakePhoto!!.launch("image/*")
            } else {
                Toast.makeText(
                    this@ReadExternalStorage,
                    "Permission not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}