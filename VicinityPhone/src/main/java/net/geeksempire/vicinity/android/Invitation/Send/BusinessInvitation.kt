/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 11/8/20 9:53 AM
 * Last modified 11/8/20 9:53 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.Invitation.Send

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import net.geeksempire.vicinity.android.AccountManager.Utils.UserInformationIO
import net.geeksempire.vicinity.android.Invitation.Utils.InvitationConstant
import net.geeksempire.vicinity.android.Invitation.Utils.ShareIt
import net.geeksempire.vicinity.android.R
import net.geeksempire.vicinity.android.Utils.UI.NotifyUser.SnackbarActionHandlerInterface
import net.geeksempire.vicinity.android.Utils.UI.NotifyUser.SnackbarBuilder

class BusinessInvitation (val context: Context, val rootView: ViewGroup) {

    private val userInformationIO = UserInformationIO(context)

    fun invite(firebaseUser: FirebaseUser) {

        val dynamicLink = Firebase.dynamicLinks.dynamicLink {

            link = Uri.parse("https://www.geeksempire.net/VicinityInvitation.html")
                .buildUpon()
                .appendQueryParameter(InvitationConstant.InvitationType, userInformationIO.readAccountType())
                .appendQueryParameter(InvitationConstant.UniqueUserId, firebaseUser.uid)
                .appendQueryParameter(InvitationConstant.UserDisplayName, firebaseUser.displayName)
                .appendQueryParameter(InvitationConstant.UserProfileImage, firebaseUser.photoUrl.toString())
                .build()

            domainUriPrefix = "https://geeksempire.page.link"

            socialMetaTagParameters {

            }

            androidParameters("net.geeksempire.vicinity".plus(".android")) {

            }

            iosParameters("net.geeksempire.vicinity".plus(".ios")) {

            }

        }

        val dynamicLinkUri = dynamicLink.uri

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newHtmlText(
            firebaseUser.displayName,
            InvitationConstant.generateBusinessInvitationText(dynamicLinkUri, firebaseUser.displayName.toString()),
            InvitationConstant.generateBusinessInvitationHtmlText(dynamicLinkUri, firebaseUser.displayName.toString())
        )
        clipboardManager.setPrimaryClip(clipData).also {

            SnackbarBuilder(context).show (
                rootView = rootView,
                messageText= context.getString(R.string.invitationDataReady),
                messageDuration = Snackbar.LENGTH_INDEFINITE,
                actionButtonText = R.string.inviteAction,
                snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                    override fun onActionButtonClicked(snackbar: Snackbar) {
                        super.onActionButtonClicked(snackbar)

                        ShareIt(context)
                            .invoke(InvitationConstant.generateBusinessInvitationText(dynamicLinkUri, firebaseUser.displayName.toString()))

                    }

                }
            )


        }

    }

}