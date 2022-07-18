package com.plcoding.androidstorage.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.plcoding.androidstorage.FirebaseUpload


var locationListener: LocationListener = object : LocationListener {
    override fun onLocationChanged(location: Location) {
        val latitude: Double = location.latitude
        val longitude: Double = location.longitude
        // Push your location to FireBase
        FirebaseUpload.locationToDatabase(latitude, longitude )
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}
}

