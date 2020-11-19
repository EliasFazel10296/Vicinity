/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 11/19/20 11:39 AM
 * Last modified 11/19/20 11:31 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.AccountManager.UserState

import com.google.firebase.firestore.FirebaseFirestore
import net.geeksempire.vicinity.android.AccountManager.Utils.UserInformation

class OnlineOffline (private val firestoreDatabase: FirebaseFirestore) {

    fun startUserStateProcess(vicinityDatabasePath: String, userIdentification: String, userOnlineOfflineState: Boolean) {

        firestoreDatabase
            .document(UserInformation.uniqueUserInformationDatabasePath(vicinityDatabasePath, userIdentification))
            .update(
                "userState", userOnlineOfflineState,
            ).addOnSuccessListener {



            }

        firestoreDatabase
            .document(UserInformation.userProfileDatabasePath(userIdentification))
            .update(
                "userState", userOnlineOfflineState,
            ).addOnSuccessListener {



            }

    }

}