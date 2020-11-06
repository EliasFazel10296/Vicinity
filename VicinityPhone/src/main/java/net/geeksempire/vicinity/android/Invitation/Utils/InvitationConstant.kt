/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 11/6/20 7:41 AM
 * Last modified 11/6/20 7:41 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.Invitation.Utils

import android.net.Uri

class InvitationConstant {

    object InvitationTypes {
        const val Business: String = "Business"
        const val Personal: String = "Personal"
    }

    companion object {
        const val InvitationType: String = "InvitationType"

        const val UniqueUserId: String = "UniqueUserId"

        fun generateBusinessInvitationText(dynamicLinkUri: Uri, businessDisplayName: String) : String {

            return "Chat With Us On Vicinity | ${businessDisplayName}" +
                    "\n\n" +
                    "${dynamicLinkUri}"
        }

        fun generateBusinessInvitationHtmlText(dynamicLinkUri: Uri, businessDisplayName: String) : String {

            return "Chat With Us On Vicinity | <b>${businessDisplayName}</b>" +
                    "<br/><br/>" +
                    "${dynamicLinkUri}"
        }

        fun generatePersonalInvitationText(dynamicLinkUri: Uri) : String {

            return "" +
                    "\n\n" +
                    "${dynamicLinkUri}"
        }

        fun generatePersonalInvitationHtmlText(dynamicLinkUri: Uri, businessDisplayName: String) : String {

            return "Chat With Us On Vicinity | ${businessDisplayName}" +
                    "\n\n" +
                    "${dynamicLinkUri}"
        }

    }

}