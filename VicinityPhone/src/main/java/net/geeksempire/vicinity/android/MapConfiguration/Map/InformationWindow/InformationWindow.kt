/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 10/18/20 9:29 AM
 * Last modified 10/18/20 9:18 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.MapConfiguration.Map.InformationWindow

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.View
import com.google.android.gms.maps.model.Marker
import net.geeksempire.vicinity.android.AccountManager.DataStructure.UserInformationDataStructure
import net.geeksempire.vicinity.android.CommunicationConfiguration.Private.PrivateMessengerUI.PrivateMessenger
import net.geeksempire.vicinity.android.CommunicationConfiguration.Private.Utils.privateMessengerName
import net.geeksempire.vicinity.android.MapConfiguration.Map.MapsOfSociety
import net.geeksempire.vicinity.android.R
import net.geeksempire.vicinity.android.Utils.UI.Theme.ThemeType
import net.geeksempire.vicinity.android.databinding.GoogleMapInformationWindowBinding
import java.util.*

class InformationWindow (private val context: MapsOfSociety) {

    private val googleMapInformationWindowBinding =  GoogleMapInformationWindowBinding.inflate(context.layoutInflater)

    var informationWindowData: InformationWindowData? = null

    init {

        when (context.overallTheme.checkThemeLightDark()) {
            ThemeType.ThemeLight -> {

                googleMapInformationWindowBinding.rootView.setBackgroundColor(context.getColor(R.color.light_blurry_color))

                val contentWindowDrawable = context.getDrawable(R.drawable.information_window_content_background)
                contentWindowDrawable?.setTint(context.getColor(R.color.light))

                googleMapInformationWindowBinding.contentContainer.background = (contentWindowDrawable)

                googleMapInformationWindowBinding.userDisplayName.setTextColor(context.getColor(R.color.dark))

                googleMapInformationWindowBinding.instagramAddressView.setTextColor(context.getColor(R.color.dark))
                googleMapInformationWindowBinding.instagramAddressLayout.boxBackgroundColor = context.getColor(R.color.white)

                googleMapInformationWindowBinding.twitterAddressView.setTextColor(context.getColor(R.color.dark))
                googleMapInformationWindowBinding.twitterAddressLayout.boxBackgroundColor = context.getColor(R.color.white)

                googleMapInformationWindowBinding.phoneNumberAddressView.setTextColor(context.getColor(R.color.dark))
                googleMapInformationWindowBinding.phoneNumberAddressLayout.boxBackgroundColor = context.getColor(R.color.white)

            }
            ThemeType.ThemeDark -> {

                googleMapInformationWindowBinding.rootView.setBackgroundColor(context.getColor(R.color.dark_blurry_color))

                val contentWindowDrawable = context.getDrawable(R.drawable.information_window_content_background)
                contentWindowDrawable?.setTint(context.getColor(R.color.dark))

                googleMapInformationWindowBinding.contentContainer.background = (contentWindowDrawable)

                googleMapInformationWindowBinding.userDisplayName.setTextColor(context.getColor(R.color.light))

                googleMapInformationWindowBinding.instagramAddressView.setTextColor(context.getColor(R.color.light))
                googleMapInformationWindowBinding.instagramAddressLayout.boxBackgroundColor = context.getColor(R.color.black)

                googleMapInformationWindowBinding.twitterAddressView.setTextColor(context.getColor(R.color.light))
                googleMapInformationWindowBinding.twitterAddressLayout.boxBackgroundColor = context.getColor(R.color.black)

                googleMapInformationWindowBinding.phoneNumberAddressView.setTextColor(context.getColor(R.color.light))
                googleMapInformationWindowBinding.phoneNumberAddressLayout.boxBackgroundColor = context.getColor(R.color.black)

            }
        }

    }

    fun setUpContentContents(marker: Marker) {

        informationWindowData?.let { informationWindowData ->

            if (informationWindowData.userDocument[UserInformationDataStructure.userIdentification] == context.firebaseUser.uid) {

                googleMapInformationWindowBinding.userDisplayName.text = Html.fromHtml("${informationWindowData.userDocument[UserInformationDataStructure.userDisplayName].toString()} <small> | You Are Here!</small>", Html.FROM_HTML_MODE_LEGACY)

            } else {

                googleMapInformationWindowBinding.userDisplayName.text = Html.fromHtml(informationWindowData.userDocument[UserInformationDataStructure.userDisplayName].toString(), Html.FROM_HTML_MODE_LEGACY)

            }

            //Instagram
            if (informationWindowData.userDocument[UserInformationDataStructure.instagramAccount].toString().isNotEmpty()) {

                googleMapInformationWindowBinding.instagramAddressLayout.visibility = View.VISIBLE
                googleMapInformationWindowBinding.instagramLogo.visibility = View.VISIBLE

                googleMapInformationWindowBinding.instagramAddressLayout.hint = "${informationWindowData.userDocument[UserInformationDataStructure.userDisplayName]}'s Instagram"
                googleMapInformationWindowBinding.instagramAddressView.setText(informationWindowData.userDocument[UserInformationDataStructure.instagramAccount].toString().toLowerCase(
                    Locale.getDefault()))

                clickOnProfileInformation(
                    googleMapInformationWindowBinding.instagramLogo,
                    "https://instagram.com/${informationWindowData.userDocument[UserInformationDataStructure.instagramAccount].toString()}"
                )

            }

            //Twitter
            if (informationWindowData.userDocument[UserInformationDataStructure.twitterAccount].toString().isNotEmpty()) {

                googleMapInformationWindowBinding.twitterAddressLayout.visibility = View.VISIBLE
                googleMapInformationWindowBinding.twitterLogo.visibility = View.VISIBLE

                googleMapInformationWindowBinding.twitterAddressLayout.hint = "${informationWindowData.userDocument[UserInformationDataStructure.userDisplayName]}'s Twitter"
                googleMapInformationWindowBinding.twitterAddressView.setText(informationWindowData.userDocument[UserInformationDataStructure.twitterAccount].toString())

                clickOnProfileInformation(
                    googleMapInformationWindowBinding.twitterLogo,
                    "https://twitter.com/${informationWindowData.userDocument[UserInformationDataStructure.twitterAccount].toString()}"
                )

            }

            //Phone Number
            if (informationWindowData.userDocument[UserInformationDataStructure.phoneNumber].toString().isNotEmpty()) {

                googleMapInformationWindowBinding.phoneNumberAddressLayout.visibility = View.VISIBLE
                googleMapInformationWindowBinding.phoneNumberLogo.visibility = View.VISIBLE

                googleMapInformationWindowBinding.phoneNumberAddressLayout.hint = "${informationWindowData.userDocument[UserInformationDataStructure.userDisplayName]}'s Phone"
                googleMapInformationWindowBinding.phoneNumberAddressView.setText(informationWindowData.userDocument[UserInformationDataStructure.phoneNumber].toString().plus(if (informationWindowData.userDocument[UserInformationDataStructure.phoneNumberVerified].toString().toBoolean()) { " ✔" } else { "" }))

                clickOnProfileInformation(
                    googleMapInformationWindowBinding.phoneNumberLogo,
                    "tel:${informationWindowData.userDocument[UserInformationDataStructure.phoneNumber].toString()}"
                )

            }

            googleMapInformationWindowBinding.rootView.setOnClickListener {

                context.mapsViewBinding.informationWindowContainer.visibility = View.GONE

                context.mapsViewBinding.informationWindowContainer.removeAllViews()

            }

            if (context.firebaseUser.uid != informationWindowData.userDocument[UserInformationDataStructure.userIdentification].toString()) {

                googleMapInformationWindowBinding.enterPrivateChat.visibility = View.VISIBLE

                googleMapInformationWindowBinding.enterPrivateChat.playAnimation()

            } else {

                googleMapInformationWindowBinding.enterPrivateChat.visibility = View.INVISIBLE

            }

            googleMapInformationWindowBinding.enterPrivateChat.setOnClickListener {

                val selfUid = context.firebaseUser.uid
                val selfUsername = context.firebaseUser.displayName
                val selfProfileImage = context.firebaseUser.photoUrl.toString()

                val otherUid = "${informationWindowData.userDocument[UserInformationDataStructure.userIdentification]}"
                val otherUsername = "${informationWindowData.userDocument[UserInformationDataStructure.userDisplayName]}"
                val otherProfileImage = "${informationWindowData.userDocument[UserInformationDataStructure.userProfileImage]}"

                val privateMessengerName = privateMessengerName(selfUid, otherUid)

                PrivateMessenger.open(
                    context = context,
                    privateMessengerName = privateMessengerName,
                    otherUid = otherUid,
                    otherUsername = otherUsername,
                    otherProfileImage = otherProfileImage
                )

            }

        }

    }

    fun getRootView() : GoogleMapInformationWindowBinding {

        return googleMapInformationWindowBinding
    }

    private fun clickOnProfileInformation(view: View, addressLink: String) {

        view.setOnClickListener {

            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(addressLink)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })

        }

    }

}