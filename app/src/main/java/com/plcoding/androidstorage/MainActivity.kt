package com.plcoding.androidstorage

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.*
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.plcoding.androidstorage.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter
    private lateinit var externalStoragePhotoAdapter: SharedPhotoAdapter

    private var readPermission = false
    private var writePermission = false
    private var cameraPermission = false
    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var intentLauncher : ActivityResultLauncher<IntentSenderRequest>
    private lateinit var contentObserver : ContentObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter{
            lifecycleScope.launch {
                val isDeleted = deletePhotofromInternalStorage(it.name)
                if (isDeleted) {
                    loadPhotostoRecyclerView()
                    Toast.makeText(this@MainActivity, "Photo Deleted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this@MainActivity, "Deletion Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        externalStoragePhotoAdapter = SharedPhotoAdapter{
            lifecycleScope.launch {
                deletePhotofromExternalStorage(it.contentUri)
            }
        }

        setupExternalRecyclerView()
        initContentObserver()

//--------------------ASK FOR PERMISSIONS---------------------

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            readPermission = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermission
            writePermission = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermission
            cameraPermission = permissions[Manifest.permission.CAMERA] ?: cameraPermission

            if(readPermission){
                loadPhotosFromExternalToRecyclerView()
            }
            else{
                Toast.makeText(this, "Read Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }
        requestPermission()

//        ------intent-Launcher---------

        intentLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
            if(it.resultCode == RESULT_OK){
                Toast.makeText(this, "Photo Deleted", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Deletion Failed", Toast.LENGTH_SHORT).show()
            }
        }


//---------------Click-Picture-Button-------------------
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
            lifecycleScope.launch{
                val isPrivate = binding.switchPrivate.isChecked
                val isSaveSuccess = when{
                        isPrivate -> savePhotoToInternalStorage(UUID.randomUUID().toString(), it)
                        writePermission -> savePhotoToExternalStorage(UUID.randomUUID().toString(), it)
                        else -> false
                    }
                    if(isPrivate){
                        loadPhotostoRecyclerView()
                    }
                    if (isSaveSuccess) {
                        Toast.makeText(this@MainActivity, "Saving Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Saving Failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnTakePhoto.setOnClickListener{
            if(cameraPermission) {
                takePhoto.launch()
            }
            else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        setupRecyclerView()
        loadPhotostoRecyclerView()
        loadPhotosFromExternalToRecyclerView()
    }




    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                if(readPermission){
                    loadPhotosFromExternalToRecyclerView()
                }
            }
        }
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
    }





    private suspend fun loadPhotosFromExternalStorage(): List<SharedStoragePhoto> {
        return withContext(Dispatchers.IO){

            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            }?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )

            val photos = mutableListOf<SharedStoragePhoto>()

            contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use {  cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                while(cursor.moveToNext()){
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    photos.add(SharedStoragePhoto(id,displayName, width, height,contentUri))
                }
                photos.toList()
            }?: listOf()
        }
    }

//    ---------Requesting-Permissions----------

    private fun requestPermission(){
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermission = hasReadPermission
        writePermission = hasWritePermission || minSdk29
        cameraPermission = hasCameraPermission

        val permissionsToRequest = mutableListOf<String>()
        if (!readPermission) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!writePermission) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!cameraPermission) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }


    private fun setupRecyclerView()= binding.rvPrivatePhotos.apply {
        adapter = internalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun setupExternalRecyclerView()= binding.rvPublicPhotos.apply {
        adapter = externalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
    }


    private fun loadPhotostoRecyclerView(){
        lifecycleScope.launch {
            val photos = loadPhotosfromInternalStorage()
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    private fun loadPhotosFromExternalToRecyclerView(){
        lifecycleScope.launch {
            val photos = loadPhotosFromExternalStorage()
            externalStoragePhotoAdapter.submitList(photos)
        }
    }

    private suspend fun loadPhotosfromInternalStorage(): List<InternalStoragePhoto>{
        return withContext(Dispatchers.IO){
            val files= filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes= it.readBytes()
                val bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                InternalStoragePhoto(it.name,bitmap)
            }?: listOf()
        }
    }

    // ----------External Storage----------


    private suspend fun savePhotoToExternalStorage(displayName: String, bmp: Bitmap): Boolean {
        return withContext(Dispatchers.IO){
            val imageCollection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

// ----------STORE METADATA----------
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }

            try{
                contentResolver.insert(imageCollection, contentValues)?.also { uri->
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        if(!bmp.compress(Bitmap.CompressFormat.JPEG, 93, outputStream)){
                            throw IOException("Could not save image")
                        }
                    }
                }?: throw IOException("Failed to save image")
                true
            }catch (e: Exception){
                e.printStackTrace()
                false
            }
        }
    }


    // ----------Internal Storage----------

    private suspend fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 93, stream)) {
                        throw IOException("Error saving bitmap")
                    }
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun deletePhotofromInternalStorage(filename: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                deleteFile(filename)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun deletePhotofromExternalStorage(photoUri: Uri){
        withContext(Dispatchers.IO){
            try {
                contentResolver.delete(photoUri,null,null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(contentResolver, listOf(photoUri)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableException = e as? RecoverableSecurityException
                        recoverableException?.userAction?.actionIntent?.intentSender
                    }
                    else -> {
                        null
                    }
                }
                intentSender?.let { sender ->
                    intentLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }


//    ------------Destroy Function------------
    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
    }

}