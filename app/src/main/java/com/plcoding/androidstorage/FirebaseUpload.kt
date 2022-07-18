package com.plcoding.androidstorage

import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*


class FirebaseUpload() {

    companion object{
        private lateinit var storageReference: StorageReference
        private lateinit var imageUri: Uri
        private val database = FirebaseDatabase.getInstance()
        private val myRef = database.reference
        fun uploadPicture(imageUris: Uri? = null) {

            imageUri = Uri.parse(imageUris.toString())
            storageReference = FirebaseStorage.getInstance().getReference("Images/${imageUri.lastPathSegment}")
            storageReference.putFile(imageUri).addOnSuccessListener {
                Log.e("FirebaseUpload", "Successfully uploaded image")
            }.addOnFailureListener{
                Log.e("Error", "Failed to upload image")
            }
        }

        fun locationToDatabase(longitude: Double, latitude: Double) {
            val currentTime: Date = Calendar.getInstance().time
            val usersRef = myRef.child("User Locations")
            val location= "Longitude: $longitude, Latitude:$latitude"
            usersRef.child("$currentTime").setValue(location)
        }
    }
}