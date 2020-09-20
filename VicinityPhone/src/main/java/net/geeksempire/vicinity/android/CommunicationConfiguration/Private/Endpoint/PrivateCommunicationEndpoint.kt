/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 9/20/20 9:10 AM
 * Last modified 9/20/20 8:29 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.CommunicationConfiguration.Private.Endpoint

import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.vicinityName

class PrivateCommunicationEndpoint {

//    val privateChatName: String = firebaseUser.uid + "|" + markerClick.title
//    val privateChatNameReverse: String = markerClick.title + "|" + firebaseUser.uid

    // if (documentSnapshot.id == privateChatName) {}
    // else if (documentSnapshot.id == reversePrivateChatName) {}
    // else {}

    companion object {

        /**
         * Collection Path: Odd
         * Document Path : Even
         **/
        private const val commonPrivateEndpoint: String = "Vicinity/OnlineSociety/Private/Messenger/"

        fun privateMessengerDocumentEndpoint(privateMessengerName: String) : String {

            return PrivateCommunicationEndpoint.commonPrivateEndpoint + privateMessengerName + "/" + vicinityName(locationLatitudeLongitude)
        }

        fun privateMessengerCollectionEndpoint(privateMessengerName: String) : String {

            return PrivateCommunicationEndpoint.commonPrivateEndpoint + privateMessengerName
        }

        //  Vicinity/OnlineSociety/Private/Messenger/[uid]/

    }
}