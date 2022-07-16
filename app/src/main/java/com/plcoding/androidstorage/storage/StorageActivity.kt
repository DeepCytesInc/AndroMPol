package com.plcoding.androidstorage.storage

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.ActivityResultCallback
import com.plcoding.androidstorage.storage.ReadExternalStorage
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Button
import com.plcoding.androidstorage.databinding.ActivityStorageBinding
import java.util.ArrayList

//import com.example.scopedstoragejavayt.databinding.ActivityMainBinding;
class StorageActivity : AppCompatActivity() {
    private val Button1: Button? = null

    //Once view binding is enabled in a module, it generates a binding class for each XML layout file present in that module
    private var binding: ActivityStorageBinding? = null
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    var mPermissionResultLauncher: ActivityResultLauncher<Array<String>>? = null
    var mGetImage: ActivityResultLauncher<Intent>? = null
    var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        //for giving multiple permissions
        mPermissionResultLauncher = registerForActivityResult(
            RequestMultiplePermissions(),
            ActivityResultCallback<Map<String?, Boolean?>> { result -> //
                if (result[Manifest.permission.READ_EXTERNAL_STORAGE] != null) {
                    isReadPermissionGranted = result[Manifest.permission.READ_EXTERNAL_STORAGE]!!
                }
                if (result[Manifest.permission.WRITE_EXTERNAL_STORAGE] != null) {
                    isWritePermissionGranted = result[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!
                }
            })

//        mGetImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//
//                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
//
//                    Bundle bundle = result.getData().getExtras();
//                    Bitmap bitmap = (Bitmap) bundle.get("data");
//
//                    if (isWritePermissionGranted){
//
//                        if (saveImageToExternalStorage(UUID.randomUUID().toString(),bitmap)){
//
//                            Toast.makeText(MainActivity.this,"saved Image Successfully" ,Toast.LENGTH_SHORT).show();
//
//                        }
//
//                    }else {
//
//                        Toast.makeText(MainActivity.this,"Permission not granted" ,Toast.LENGTH_SHORT).show();
//
//
//                    }
//
//                }
//
//            }
//        });
        requestPermission()
        binding!!.Button1.setOnClickListener {
            startActivity(
                Intent(
                    this@StorageActivity,
                    ReadExternalStorage::class.java
                )
            )
        }
    }

    private fun requestPermission() {
        val minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        isWritePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        isWritePermissionGranted = isWritePermissionGranted || minSDK
        val permissionRequest: MutableList<String> = ArrayList()
        if (!isReadPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!isWritePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!permissionRequest.isEmpty()) {
            mPermissionResultLauncher!!.launch(permissionRequest.toTypedArray())
        }
    } //    private boolean saveImageToExternalStorage(String imgName, Bitmap bmp){

    //
    //        Uri ImageCollection = null;
    //        ContentResolver resolver = getContentResolver();
    //
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
    //
    //            ImageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
    //
    //        }else {
    //
    //            ImageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    //        }
    //
    //        ContentValues contentValues = new ContentValues();
    //        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imgName + ".jpg");
    //        contentValues.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
    //        Uri imageUri = resolver.insert(ImageCollection, contentValues);
    //
    //        try {
    //
    //            OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
    //            bmp.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
    //            Objects.requireNonNull(outputStream);
    //            return true;
    //
    //        }
    //        catch (Exception e){
    //
    //            Toast.makeText(this,"Image not saved: \n" + e,Toast.LENGTH_SHORT).show();
    //            e.printStackTrace();
    //
    //        }
    //
    //        return false;
    //
    //
    //    }
    companion object {
        private const val CAMERA = 100
    }
}