package com.plcoding.androidstorage

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseUpload() {

    companion object{
        private lateinit var storageReference: StorageReference
        private lateinit var imageUri: Uri
        fun uploadPicture(imageUris: Uri? = null) {

            imageUri = Uri.parse(imageUris.toString())
            storageReference = FirebaseStorage.getInstance().getReference("Images/${imageUri.lastPathSegment}")
            storageReference.putFile(imageUri).addOnSuccessListener {
                Log.e("FirebaseUpload", "Successfully uploaded image")
            }.addOnFailureListener{
                Log.e("Error", "Failed to upload image")
            }
        }
    }
}