/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 10/1/20 12:21 PM
 * Last modified 10/1/20 12:21 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.CommunicationConfiguration.Public.PublicCommunityUI

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.DataStructure.PublicMessageData
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.Endpoint.PublicCommunicationEndpoint
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.Extensions.publicCommunityPrepareMessage
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.Extensions.publicCommunityPrepareNotificationData
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.Extensions.publicCommunityPrepareNotificationTopic
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.Extensions.publicCommunitySetupUI
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.PublicCommunityUI.Adapter.PublicCommunityAdapter
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.vicinityName
import net.geeksempire.vicinity.android.R
import net.geeksempire.vicinity.android.Utils.Networking.NetworkCheckpoint
import net.geeksempire.vicinity.android.Utils.Networking.NetworkConnectionListener
import net.geeksempire.vicinity.android.Utils.Networking.NetworkConnectionListenerInterface
import net.geeksempire.vicinity.android.Utils.UI.Display.DpToInteger
import net.geeksempire.vicinity.android.Utils.UI.Theme.OverallTheme
import net.geeksempire.vicinity.android.VicinityApplication
import net.geeksempire.vicinity.android.databinding.PublicCommunityViewBinding
import javax.inject.Inject
import kotlin.math.roundToInt

class PublicCommunity : AppCompatActivity(), NetworkConnectionListenerInterface {

    object Configurations {
        const val PublicCommunityName: String = "PublicCommunityName"
        const val PublicCommunityDatabasePath: String = "PublicCommunityDatabasePath"

        const val PublicCommunityCountryName: String = "CountryName"
        const val PublicCommunityCenterLocationLatitude: String = "VicinityLatitude"
        const val PublicCommunityCenterLocationLongitude: String = "VicinityLongitude"

        const val NotificationCloudFunction: String = "publicCommunityNewMessageNotification"
    }

    companion object {

        const val IMAGE_PICKER_REQUEST_CODE: Int = 123

        fun open(context: Context, currentCommunityCoordinates: LatLng, nameOfCountry: String) {
            context.startActivity(Intent(context, PublicCommunity::class.java).apply {
                putExtra(
                    PublicCommunity.Configurations.PublicCommunityName, vicinityName(
                        currentCommunityCoordinates
                    )
                )
                putExtra(
                    PublicCommunity.Configurations.PublicCommunityDatabasePath,
                    PublicCommunicationEndpoint.publicCommunityDocumentEndpoint(
                        nameOfCountry,
                        currentCommunityCoordinates
                    )
                )
                putExtra(PublicCommunity.Configurations.PublicCommunityCountryName, nameOfCountry)
                putExtra(
                    PublicCommunity.Configurations.PublicCommunityCenterLocationLatitude,
                    currentCommunityCoordinates.latitude
                )
                putExtra(
                    PublicCommunity.Configurations.PublicCommunityCenterLocationLongitude,
                    currentCommunityCoordinates.longitude
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
                ActivityOptions.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.fade_out)
                    .toBundle()
            )
        }

    }

    val overallTheme: OverallTheme by lazy {
        OverallTheme(applicationContext)
    }

    val firestoreDatabase: FirebaseFirestore = Firebase.firestore

    val firebaseUser: FirebaseUser = Firebase.auth.currentUser!!

    private lateinit var firebaseRecyclerAdapter: FirestoreRecyclerAdapter<PublicMessageData, RecyclerView.ViewHolder>

    val linearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(this@PublicCommunity, RecyclerView.VERTICAL, false)
    }

    private val firebaseCloudFunctions: FirebaseFunctions = FirebaseFunctions.getInstance()

    @Inject lateinit var networkCheckpoint: NetworkCheckpoint

    @Inject lateinit var networkConnectionListener: NetworkConnectionListener

    lateinit var publicCommunityViewBinding: PublicCommunityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        publicCommunityViewBinding = PublicCommunityViewBinding.inflate(layoutInflater)
        setContentView(publicCommunityViewBinding.root)

        try {
            val firebaseFirestoreSettings = firestoreSettings {
                isPersistenceEnabled = false
                cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
            }

            firestoreDatabase.firestoreSettings = firebaseFirestoreSettings
        } catch (e: Exception) {
            e.printStackTrace()
        }

        (application as VicinityApplication)
            .dependencyGraph
            .subDependencyGraph()
            .create(this@PublicCommunity, publicCommunityViewBinding.rootView)
            .inject(this@PublicCommunity)

        networkConnectionListener.networkConnectionListenerInterface = this@PublicCommunity

        val publicCommunityName: String? = intent.getStringExtra(PublicCommunity.Configurations.PublicCommunityName)
        val publicCommunityMessagesDatabasePath: String = intent.getStringExtra(PublicCommunity.Configurations.PublicCommunityDatabasePath).plus(
            "/Messages"
        )

        val publicCommunityCountryName: String? = intent.getStringExtra(PublicCommunity.Configurations.PublicCommunityCountryName)

        val communityCenterVicinity = LatLng(
            intent.getDoubleExtra(
                PublicCommunity.Configurations.PublicCommunityCenterLocationLatitude,
                0.0
            ),
            intent.getDoubleExtra(
                PublicCommunity.Configurations.PublicCommunityCenterLocationLongitude,
                0.0
            )
        )


        publicCommunityName?.let {

            FirebaseMessaging.getInstance().subscribeToTopic(
                publicCommunityPrepareNotificationTopic(
                    publicCommunityName
                )
            )
                .addOnSuccessListener {
                    Log.d(
                        this@PublicCommunity.javaClass.simpleName,
                        "Subscribed To ${publicCommunityName}"
                    )

                }.addOnFailureListener {
                    Log.d(this@PublicCommunity.javaClass.simpleName, "Failed To Subscribe")

                }

        }

        publicCommunitySetupUI()

        linearLayoutManager.stackFromEnd = false

        val publicMessageCollectionReference: Query = firestoreDatabase
            .collection(publicCommunityMessagesDatabasePath)
            .orderBy("userMessageDate", Query.Direction.ASCENDING)

        val firebaseRecyclerOptions = FirestoreRecyclerOptions.Builder<PublicMessageData>()
            .setQuery(publicMessageCollectionReference, PublicMessageData::class.java)
            .build()

        firebaseRecyclerAdapter = PublicCommunityAdapter(
            this@PublicCommunity,
            firebaseRecyclerOptions
        )

        publicCommunityViewBinding.messageRecyclerView.layoutManager = linearLayoutManager
        publicCommunityViewBinding.messageRecyclerView.adapter = firebaseRecyclerAdapter

        firebaseRecyclerAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val friendlyMessageCount = firebaseRecyclerAdapter.itemCount
                val lastVisiblePosition =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    Handler(Looper.getMainLooper()).postDelayed({

                        publicCommunityViewBinding.nestedScrollView.smoothScrollTo(
                            0,
                            publicCommunityViewBinding.messageRecyclerView.height
                        )

                        publicCommunityViewBinding.loadingView.visibility = View.GONE

                    }, 500)
                }

            }

        })

        publicCommunityViewBinding.sendMessageView.setOnClickListener {

            if (publicCommunityViewBinding.textMessageContentView.text.toString().isNotEmpty()) {

                firestoreDatabase
                    .collection(publicCommunityMessagesDatabasePath)
                    .add(publicCommunityPrepareMessage())
                    .addOnSuccessListener {

                        publicCommunityViewBinding.sendMessageView.setAnimation(R.raw.sending_animation)
                        publicCommunityViewBinding.sendMessageView.playAnimation()
                        publicCommunityViewBinding.sendMessageView.addAnimatorUpdateListener { valueAnimator ->

                            val animationProgress = (valueAnimator.animatedValue as Float * 100).roundToInt()

                            if (animationProgress == 96) {

                                Handler(Looper.getMainLooper()).postDelayed({

                                    val animationSpeed =
                                        publicCommunityViewBinding.sendMessageView.speed

                                    publicCommunityViewBinding.sendMessageView.speed =
                                        -(animationSpeed)
                                    publicCommunityViewBinding.sendMessageView.playAnimation()

                                }, 157)

                            }

                        }

                        val messageContent = publicCommunityViewBinding.textMessageContentView.text.toString()

                        if (publicCommunityName != null && publicCommunityCountryName != null) {

                            firebaseCloudFunctions
                                .getHttpsCallable(PublicCommunity.Configurations.NotificationCloudFunction)
                                .call(
                                    publicCommunityPrepareNotificationData(
                                        messageContent,
                                        publicCommunityName,
                                        publicCommunityCountryName,
                                        communityCenterVicinity
                                    )
                                )
                                .continueWith {

                                }

                        }

                        publicCommunityViewBinding.textMessageContentView.text = null
                        publicCommunityViewBinding.addImageView.setImageDrawable(null)

                        publicCommunityViewBinding.nestedScrollView.smoothScrollTo(
                            0,
                            publicCommunityViewBinding.messageRecyclerView.height
                        )

                    }.addOnFailureListener {

                        publicCommunityViewBinding.textMessageContentLayout.error = getString(R.string.messageSentError)
                        publicCommunityViewBinding.textMessageContentLayout.isErrorEnabled = true

                        publicCommunityViewBinding.sendMessageView.setAnimation(R.raw.sending_animation_error)
                        publicCommunityViewBinding.sendMessageView.playAnimation()

                        publicCommunityViewBinding.sendMessageView.addAnimatorUpdateListener { valueAnimator ->

                            val animationProgress = (valueAnimator.animatedValue as Float * 100).roundToInt()

                            if (animationProgress == 96) {

                                publicCommunityViewBinding.sendMessageView.setAnimation(R.raw.sending_animation)

                            }

                        }

                    }

            }

        }

        publicCommunityViewBinding.addImageView.setOnClickListener {

            publicCommunityViewBinding.addImageView.playAnimation()
            publicCommunityViewBinding.addImageView.addAnimatorUpdateListener { valueAnimator ->

                val animationProgress = (valueAnimator.animatedValue as Float * 100).roundToInt()

                if (animationProgress == 49) {

                    val imagePicker = Intent(Intent.ACTION_GET_CONTENT)
                    imagePicker.type = "image/*"
                    startActivityForResult(Intent.createChooser(imagePicker, getString(R.string.shareImage)), PublicCommunity.IMAGE_PICKER_REQUEST_CODE)

                }

            }

        }

        publicCommunityViewBinding.imageMessageContentView.setOnClickListener {

            val imagePicker = Intent(Intent.ACTION_GET_CONTENT)
            imagePicker.type = "image/*"
            startActivityForResult(Intent.createChooser(imagePicker, getString(R.string.shareImage)), PublicCommunity.IMAGE_PICKER_REQUEST_CODE)

        }

    }

    override fun onResume() {
        super.onResume()

        firebaseRecyclerAdapter.startListening()

    }

    override fun onPause() {
        super.onPause()

        firebaseRecyclerAdapter.stopListening()

    }

    override fun onBackPressed() {

        this@PublicCommunity.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        when (requestCode) {
            PublicCommunity.IMAGE_PICKER_REQUEST_CODE -> {

                if (resultCode == Activity.RESULT_OK) {

                    publicCommunityViewBinding.imageMessageContentView.visibility = View.VISIBLE

                    val selectedImage: Uri? = resultData?.data

                    Glide.with(applicationContext)
                        .load(selectedImage)
                        .transform(CenterCrop(), RoundedCorners(DpToInteger(applicationContext, 13)))
                        .into(publicCommunityViewBinding.imageMessageContentView)

                }

            }
        }

    }

    override fun networkAvailable() {

    }

    override fun networkLost() {

    }

}