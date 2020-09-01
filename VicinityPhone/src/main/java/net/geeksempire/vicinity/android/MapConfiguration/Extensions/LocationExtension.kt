/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 9/1/20 4:35 AM
 * Last modified 9/1/20 4:33 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.MapConfiguration.Extensions

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import net.geeksempire.chat.vicinity.Util.MapsUtil.LocationCoordinatesUpdater
import net.geeksempire.vicinity.android.MapConfiguration.Map.MapsOfSociety

fun MapsOfSociety.getLocationData() {

    val locationRequest = LocationRequest.create()
    locationRequest.interval = 1000
    locationRequest.fastestInterval = 500
    locationRequest.numUpdates = 1
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED
        && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                        location?.let {

                            userLatitudeLongitude = LatLng(location.latitude, location.longitude)

                            mapView.getMapAsync(this@getLocationData)

                        }

                    }

                }

            }

        }, null)

    }

    val locationCoordinatesUpdater = LocationCoordinatesUpdater(applicationContext, mapsLiveData)

    mapsLiveData.currentLocationData.observe(this@getLocationData, Observer { location ->

        location?.let {



        }

    })

}