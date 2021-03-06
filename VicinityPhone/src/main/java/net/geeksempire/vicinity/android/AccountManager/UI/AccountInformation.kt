/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 11/8/20 9:53 AM
 * Last modified 11/8/20 9:53 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.AccountManager.UI

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.account_view.*
import net.geeksempire.vicinity.android.AccountManager.DataStructure.UserInformationDataStructure
import net.geeksempire.vicinity.android.AccountManager.Extensions.accountManagerSetupUI
import net.geeksempire.vicinity.android.AccountManager.Extensions.createUserProfile
import net.geeksempire.vicinity.android.AccountManager.Utils.UserInformation
import net.geeksempire.vicinity.android.AccountManager.Utils.UserInformationIO
import net.geeksempire.vicinity.android.Invitation.Send.BusinessInvitation
import net.geeksempire.vicinity.android.Invitation.Utils.InvitationConstant
import net.geeksempire.vicinity.android.MapConfiguration.Map.MapsOfSociety
import net.geeksempire.vicinity.android.R
import net.geeksempire.vicinity.android.Utils.Location.LocationCheckpoint
import net.geeksempire.vicinity.android.Utils.Networking.NetworkCheckpoint
import net.geeksempire.vicinity.android.Utils.Networking.NetworkConnectionListener
import net.geeksempire.vicinity.android.Utils.Networking.NetworkConnectionListenerInterface
import net.geeksempire.vicinity.android.Utils.UI.Theme.OverallTheme
import net.geeksempire.vicinity.android.VicinityApplication
import net.geeksempire.vicinity.android.databinding.AccountViewBinding
import java.util.*
import javax.inject.Inject

class AccountInformation : AppCompatActivity(), NetworkConnectionListenerInterface {

    val overallTheme: OverallTheme by lazy {
        OverallTheme(applicationContext)
    }

    val userInformation: UserInformation by lazy {
        UserInformation(this@AccountInformation)
    }

    val userInformationIO: UserInformationIO by lazy {
        UserInformationIO(applicationContext)
    }

    val locationCheckpoint = LocationCheckpoint()

    val firebaseAuth = Firebase.auth

    val firestoreDatabase: FirebaseFirestore = Firebase.firestore

    var profileUpdating: Boolean = false

    @Inject lateinit var networkCheckpoint: NetworkCheckpoint

    @Inject lateinit var networkConnectionListener: NetworkConnectionListener

    lateinit var accountViewBinding: AccountViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewBinding = AccountViewBinding.inflate(layoutInflater)
        setContentView(accountViewBinding.root)

        accountManagerSetupUI()

        (application as VicinityApplication)
            .dependencyGraph
            .subDependencyGraph()
            .create(this@AccountInformation, accountViewBinding.rootView)
            .inject(this@AccountInformation)

        networkConnectionListener.networkConnectionListenerInterface = this@AccountInformation

        if (firebaseAuth.currentUser == null) {

            accountViewBinding.signupLoadingView.visibility = View.VISIBLE
            accountViewBinding.signupLoadingView.playAnimation()

            userInformation.startSignInProcess()

        } else {

            firebaseAuth.currentUser?.let { firebaseUser ->

                firestoreDatabase
                    .document(UserInformation.userProfileDatabasePath(firebaseUser.uid))
                    .get()
                    .addOnSuccessListener { documentSnapshot ->

                        documentSnapshot?.let { documentData ->

                            accountViewBinding.socialMediaScrollView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                            accountViewBinding.socialMediaScrollView.visibility = View.VISIBLE

                            accountViewBinding.instagramAddressView.setText(documentData.data?.get(UserInformationDataStructure.instagramAccount).toString().toLowerCase(Locale.getDefault()))

                            accountViewBinding.twitterAddressView.setText(documentData.data?.get(UserInformationDataStructure.twitterAccount).toString())

                            accountViewBinding.phoneNumberAddressView.setText(documentData.data?.get(UserInformationDataStructure.phoneNumber).toString())

                            accountViewBinding.inviteDirectlyPrivateMessage.visibility = View.VISIBLE
                            accountViewBinding.inviteDirectlyPrivateMessage.setOnClickListener {

                                BusinessInvitation(applicationContext, accountViewBinding.rootView)
                                    .invite(firebaseUser)

                            }

                            accountViewBinding.accountTypeText.text = userInformationIO.readAccountType()
                            accountViewBinding.accountTypeCheckbox.setOnClickListener {

                                accountViewBinding.accountTypeText.text = userInformationIO.readAccountType()

                                firestoreDatabase
                                    .document(UserInformation.userProfileDatabasePath(firebaseUser.uid))
                                    .update(
                                        "accountType", userInformationIO.readAccountType(),
                                    )

                                when (userInformationIO.readAccountType()) {
                                    InvitationConstant.InvitationTypes.Personal -> {

                                        userInformationIO.saveAccountType(InvitationConstant.InvitationTypes.Business)

                                        accountViewBinding.accountTypeCheckbox.setAnimation(R.raw.business_account_animation)
                                        accountViewBinding.accountTypeCheckbox.playAnimation()

                                    }
                                    InvitationConstant.InvitationTypes.Business -> {

                                        userInformationIO.saveAccountType(InvitationConstant.InvitationTypes.Personal)

                                        accountViewBinding.accountTypeCheckbox.setAnimation(R.raw.personal_account_animation)
                                        accountViewBinding.accountTypeCheckbox.playAnimation()

                                    }
                                    else -> {


                                        accountViewBinding.accountTypeCheckbox.playAnimation()

                                    }
                                }

                            }

                            accountTypeCheckbox.setOnLongClickListener {

                                Toast.makeText(applicationContext, getString(R.string.switchAccountType), Toast.LENGTH_LONG).show()

                                true
                            }

                        }

                    }

            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {

            when (requestCode) {
                UserInformation.GoogleSignInRequestCode -> {

                    val googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                    googleSignInAccountTask.addOnSuccessListener {

                        val googleSignInAccount = googleSignInAccountTask.getResult(ApiException::class.java)

                        val authCredential = GoogleAuthProvider.getCredential(googleSignInAccount?.idToken, null)
                        firebaseAuth.signInWithCredential(authCredential).addOnSuccessListener {

                            val firebaseUser = firebaseAuth.currentUser

                            if (firebaseUser != null) {

                                val accountName: String = firebaseUser.email.toString()

                                userInformationIO.saveUserInformation(accountName)

                                createUserProfile()

                                startActivity(Intent(applicationContext, MapsOfSociety::class.java),
                                    ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_in_right, 0).toBundle())

                            }

                        }.addOnFailureListener {


                        }

                    }.addOnFailureListener {
                        it.printStackTrace()

                    }

                }
                else -> {

                }
            }

        }

    }

    override fun onBackPressed() {

        if (profileUpdating) {

            profileUpdating = false

            this@AccountInformation.finish()

        } else {

            this@AccountInformation.finish()

        }

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

    }

    override fun networkAvailable() {

    }

    override fun networkLost() {

    }

}