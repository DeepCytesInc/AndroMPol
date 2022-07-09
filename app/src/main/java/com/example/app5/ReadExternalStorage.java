package com.example.app5;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.app5.databinding.ActivityReadExternalStorageBinding;

public class ReadExternalStorage extends AppCompatActivity{

    //Once view binding is enabled in a module, it generates a binding class for each XML layout file present in that module
    private ActivityReadExternalStorageBinding binding;
    private ActivityResultLauncher<String> nTakePhoto;
    private Button selectImagebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadExternalStorageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }

    private void init() {
        nTakePhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {

                        binding.firebaseimage.setImageURI(result);

                    }
                }
        );

        binding.selectImagebtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(
                        ReadExternalStorage.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                )
                {
                    nTakePhoto.launch("image/*");
                }

                else
                {
                    Toast.makeText(ReadExternalStorage.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }


            }
        }
        );
                
    }
}
