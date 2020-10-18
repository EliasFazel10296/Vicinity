/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 10/18/20 5:18 AM
 * Last modified 10/18/20 5:18 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.CommunicationConfiguration.HistoryConfiguration.DataStructure

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import net.geeksempire.vicinity.android.AccountManager.DataStructure.UserInformationVicinityArchiveData
import net.geeksempire.vicinity.android.CommunicationConfiguration.HistoryConfiguration.Endpoint.CommunicationHistoryEndpoint
import net.geeksempire.vicinity.android.CommunicationConfiguration.Private.DataStructure.PrivateMessengerData
import net.geeksempire.vicinity.android.CommunicationConfiguration.Private.DataStructure.PrivateMessengerUsersDataStructure
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.DataStructure.VicinityDataStructure

class HistoryLiveData : ViewModel() {

    val publicCommunicationHistory: MutableLiveData<ArrayList<UserInformationVicinityArchiveData>> by lazy {
        MutableLiveData<ArrayList<UserInformationVicinityArchiveData>>()
    }

    val privateCommunicationHistory: MutableLiveData<ArrayList<PrivateMessengerData>> by lazy {
        MutableLiveData<ArrayList<PrivateMessengerData>>()
    }

    fun publicHistoryNetworkOperation() {

        val publicMessengerData: ArrayList<UserInformationVicinityArchiveData> = ArrayList<UserInformationVicinityArchiveData>()

        Firebase.firestore
            .collection(CommunicationHistoryEndpoint.publicVicinityArchiveDatabasePath(Firebase.auth.currentUser!!.uid))
            .get()
            .addOnSuccessListener {

                it.forEach { documentSnapshot ->

                    publicMessengerData.add(UserInformationVicinityArchiveData(
                        vicinityCountry = documentSnapshot[VicinityDataStructure.vicinityCountry].toString(),
                        vicinityName = documentSnapshot[VicinityDataStructure.vicinityCountry].toString(),
                        vicinityLatitude = documentSnapshot[VicinityDataStructure.vicinityCountry].toString(),
                        vicinityLongitude = documentSnapshot[VicinityDataStructure.vicinityCountry].toString(),
                        lastLatitude = documentSnapshot[VicinityDataStructure.vicinityCountry].toString(),
                        lastLongitude = documentSnapshot[VicinityDataStructure.vicinityCountry].toString()
                    ))

                }

                publicCommunicationHistory.postValue(publicMessengerData)

            }

    }

    fun privateHistoryNetworkOperation() {

        val privateMessengerData: ArrayList<PrivateMessengerData> = ArrayList<PrivateMessengerData>()

        Firebase.firestore
            .collection(CommunicationHistoryEndpoint.privateVicinityArchiveDatabasePath(Firebase.auth.currentUser!!.uid))
            .get()
            .addOnSuccessListener {

                it.forEach { documentSnapshot ->

                    privateMessengerData.add(PrivateMessengerData(
                        PersonOne = documentSnapshot[PrivateMessengerUsersDataStructure.personOne].toString(),
                        PersonOneUsername = documentSnapshot[PrivateMessengerUsersDataStructure.personOneUsername].toString(),
                        PersonTwo = documentSnapshot[PrivateMessengerUsersDataStructure.personTwo].toString(),
                        PersonTwoUsername = documentSnapshot[PrivateMessengerUsersDataStructure.personTwoUsername].toString()
                    ))

                }

                privateCommunicationHistory.postValue(privateMessengerData)

            }

    }

}