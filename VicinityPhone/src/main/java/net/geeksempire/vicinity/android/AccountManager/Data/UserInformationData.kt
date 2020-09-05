/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 9/5/20 8:30 AM
 * Last modified 9/5/20 8:09 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.AccountManager.Data

import com.google.firebase.firestore.FieldValue

data class UserInformationData (var userIdentification: String, var userEmailAddress: String, var userDisplayName: String, var userProfileImage: String,
                                var userLatitude: String, var userLongitude: String,
                                var userState: String,
                                var userLastSignIn:  FieldValue,
                                var userSignUpDate:  FieldValue = FieldValue.serverTimestamp())