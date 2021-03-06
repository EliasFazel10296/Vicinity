/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 10/13/20 12:12 PM
 * Last modified 10/13/20 12:03 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import net.geeksempire.vicinity.android.AccountManager.UI.AccountInformation
import net.geeksempire.vicinity.android.AccountManager.Utils.UserInformationIO
import net.geeksempire.vicinity.android.MapConfiguration.Map.MapsOfSociety
import net.geeksempire.vicinity.android.Utils.Networking.NetworkCheckpoint
import net.geeksempire.vicinity.android.Utils.Networking.NetworkSettingCallback
import net.geeksempire.vicinity.android.Utils.UI.NotifyUser.SnackbarActionHandlerInterface
import net.geeksempire.vicinity.android.Utils.UI.NotifyUser.SnackbarBuilder
import net.geeksempire.vicinity.android.WebView.BuiltInWebView
import net.geeksempire.vicinity.android.databinding.EntryConfigurationViewBinding
import javax.inject.Inject

class EntryConfiguration : AppCompatActivity() {

    companion object {
        const val PermissionRequestCode: Int = 123
    }

    private val userInformationIO: UserInformationIO by lazy {
        UserInformationIO(applicationContext)
    }

    @Inject
    lateinit var networkCheckpoint: NetworkCheckpoint

    private lateinit var entryConfigurationViewBinding: EntryConfigurationViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entryConfigurationViewBinding = EntryConfigurationViewBinding.inflate(layoutInflater)
        setContentView(entryConfigurationViewBinding.root)

        (application as VicinityApplication)
            .dependencyGraph
            .inject(this@EntryConfiguration)

        if (userInformationIO.readPrivacyAgreement()) {

            runtimePermission()

        } else {

            entryConfigurationViewBinding.proceedButton.visibility = View.VISIBLE
            entryConfigurationViewBinding.agreementDataView.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({

                entryConfigurationViewBinding.blurBackground.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
                entryConfigurationViewBinding.blurBackground.visibility = View.VISIBLE

            }, 333)

            entryConfigurationViewBinding.proceedButton.setOnClickListener {

                runtimePermission()

            }

            entryConfigurationViewBinding.agreementDataView.setOnClickListener {

                BuiltInWebView.show(
                    context = applicationContext,
                    linkToLoad = getString(R.string.privacyAgreementLink),
                    gradientColorOne = getColor(R.color.default_color_dark),
                    gradientColorTwo = getColor(R.color.default_color_game_dark)
                )

            }

        }

    }

    override fun onBackPressed() {

        this@EntryConfiguration.finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissionsList: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults)

        when (requestCode) {
            EntryConfiguration.PermissionRequestCode -> {

                if (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {

                    navigateToMap()

                } else {

                    runtimePermissionMessage()

                }

            }
        }

    }

    private fun runtimePermission() {

        val permissionsList = arrayListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissionsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        requestPermissions(
            permissionsList.toTypedArray(),
            EntryConfiguration.PermissionRequestCode
        )

    }

    private fun runtimePermissionMessage() {

        SnackbarBuilder(applicationContext).show (
            rootView = entryConfigurationViewBinding.rootView,
            messageText= getString(R.string.permissionMessage),
            messageDuration = Snackbar.LENGTH_INDEFINITE,
            actionButtonText = R.string.grantPermission,
            snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                override fun onActionButtonClicked(snackbar: Snackbar) {
                    super.onActionButtonClicked(snackbar)

                    runtimePermission()

                }

            }
        )

    }

    private fun navigateToMap() {

        userInformationIO.savePrivacyAgreement()

        val firebaseUser = Firebase.auth.currentUser

        if (firebaseUser == null) {

            startActivity(Intent(applicationContext, AccountInformation::class.java),
                ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_in_right, 0).toBundle())

        } else {

            if (networkCheckpoint.networkConnection()) {

                startActivity(Intent(applicationContext, MapsOfSociety::class.java),
                    ActivityOptions.makeCustomAnimation(applicationContext, R.anim.fade_in, 0).toBundle())

            } else {

                SnackbarBuilder(applicationContext).show (
                    rootView = entryConfigurationViewBinding.rootView,
                    messageText= getString(R.string.noInternetConnectionText),
                    messageDuration = Snackbar.LENGTH_INDEFINITE,
                    actionButtonText = R.string.turnOnText,
                    snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                        override fun onActionButtonClicked(snackbar: Snackbar) {
                            super.onActionButtonClicked(snackbar)

                            if (!networkCheckpoint.networkConnection()) {

                                startActivityForResult(
                                    Intent(Settings.ACTION_WIFI_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    NetworkSettingCallback.WifiSetting
                                )

                            } else {

                                snackbar.dismiss()

                            }

                        }

                    }
                )

            }

        }

    }
}