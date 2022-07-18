package com.plcoding.androidstorage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.plcoding.androidstorage.databinding.ActivityMainBinding
import com.plcoding.androidstorage.location.locationListener
import com.plcoding.androidstorage.storage.StorageActivity
import java.util.*

class MainActivity: AppCompatActivity()  {

    private var locationPermission = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            locationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: locationPermission

            if(locationPermission){
                binding.WeatherText.text = "It's Such a Great Weather!"
            }
            else{
                binding.WeatherText.text = "It's Not Such a Great Weather!"
            }

        }
        requestPermission()


        binding.camera.setOnClickListener{
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        binding.storage.setOnClickListener{
            val intent = Intent(this, StorageActivity::class.java)
            startActivity(intent)
        }
        binding.recorder.setOnClickListener{
            val intent = Intent(this, MicActivity::class.java)
            startActivity(intent)
        }

        binding.root.setOnClickListener{
            fetchLocation()
        }


    }

    private fun requestPermission() {
        val haslocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        locationPermission = haslocationPermission

        val permissionsToRequest = mutableListOf<String>()
        if (!locationPermission) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun fetchLocation() {

        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    locationListener.onLocationChanged(location)
//                    val currentTime: Date = Calendar.getInstance().time
//                    Log.d("Location", "Latitude: ${location.latitude} Longitude: ${location.longitude} Time: $currentTime")
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, locationListener)
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10f, locationListener)
            }.addOnFailureListener{
                Log.d("Location", "Location not found")
            }
    }



}


