/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 9/29/20 7:23 AM
 * Last modified 9/29/20 7:23 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.MapConfiguration.Vicinity.Operations

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import net.geeksempire.vicinity.android.AccountManager.DataStructure.UserInformationData
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.Endpoint.PublicCommunicationEndpoint
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.DataStructure.VicinityData

class JoinVicinity (private val context: Context, private val googleMap: GoogleMap, private val firestoreDatabase: FirebaseFirestore) {

    fun join(vicinityDatabasePath: String, vicinityData: VicinityData, userInformationData: UserInformationData, userLocation: LatLng) {
        Log.d(this@JoinVicinity.javaClass.simpleName, "Join Existing Vicinity")

        PublicCommunicationEndpoint.CurrentCommunityCoordinates = LatLng(vicinityData.centerLatitude.toDouble(), vicinityData.centerLongitude.toDouble())

        val vicinityUserInterface: VicinityUserInterface = VicinityUserInterface(context)

        vicinityUserInterface.drawVicinity(
            googleMap = googleMap,
            userLatitudeLongitude = LatLng(vicinityData.centerLatitude.toDouble(), vicinityData.centerLongitude.toDouble()),
            locationVicinity = LatLng(vicinityData.centerLatitude.toDouble(), vicinityData.centerLongitude.toDouble())
        )

        val vicinityUserInformation: VicinityUserInformation = VicinityUserInformation(firestoreDatabase, vicinityDatabasePath)

        vicinityUserInformation.add(userInformationData, vicinityData)

    }

}