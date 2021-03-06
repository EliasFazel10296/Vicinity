/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 12/7/20 6:02 AM
 * Last modified 12/7/20 5:59 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.MapConfiguration.Map

import android.app.ActivityOptions
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import net.geeksempire.chat.vicinity.Util.MapsUtil.LocationCoordinatesUpdater
import net.geeksempire.vicinity.android.AccountManager.UserState.OnlineOffline
import net.geeksempire.vicinity.android.AccountManager.Utils.UserInformation
import net.geeksempire.vicinity.android.AccountManager.Utils.UserInformationIO
import net.geeksempire.vicinity.android.CommunicationConfiguration.HistoryConfiguration.Endpoint.CommunicationHistoryEndpoint
import net.geeksempire.vicinity.android.CommunicationConfiguration.HistoryConfiguration.HistoryUI.HistoryLists
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.Endpoint.PublicCommunicationEndpoint
import net.geeksempire.vicinity.android.CommunicationConfiguration.Public.PublicCommunityUI.PublicCommunity
import net.geeksempire.vicinity.android.EntryConfiguration
import net.geeksempire.vicinity.android.MapConfiguration.Extensions.*
import net.geeksempire.vicinity.android.MapConfiguration.LocationDataHolder.MapsLiveData
import net.geeksempire.vicinity.android.MapConfiguration.Map.InformationWindow.InformationWindow
import net.geeksempire.vicinity.android.MapConfiguration.Map.InformationWindow.InformationWindowData
import net.geeksempire.vicinity.android.MapConfiguration.Utils.MapsMarker
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.CountryInformationInterface
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.Operations.CreateVicinity
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.Operations.JoinVicinity
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.Operations.VicinityUserInformation
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.VicinityCalculations
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.VicinityInformation
import net.geeksempire.vicinity.android.MapConfiguration.Vicinity.vicinityName
import net.geeksempire.vicinity.android.R
import net.geeksempire.vicinity.android.Utils.InApplicationUpdate.InApplicationUpdateProcess
import net.geeksempire.vicinity.android.Utils.Location.LocationCheckpoint
import net.geeksempire.vicinity.android.Utils.Networking.NetworkCheckpoint
import net.geeksempire.vicinity.android.Utils.Networking.NetworkConnectionListener
import net.geeksempire.vicinity.android.Utils.Networking.NetworkConnectionListenerInterface
import net.geeksempire.vicinity.android.Utils.System.DeviceSystemInformation
import net.geeksempire.vicinity.android.Utils.UI.NotifyUser.SnackbarActionHandlerInterface
import net.geeksempire.vicinity.android.Utils.UI.NotifyUser.SnackbarBuilder
import net.geeksempire.vicinity.android.Utils.UI.Theme.OverallTheme
import net.geeksempire.vicinity.android.VicinityApplication
import net.geeksempire.vicinity.android.databinding.MapsViewBinding
import javax.inject.Inject

class MapsOfSociety : AppCompatActivity(), NetworkConnectionListenerInterface,
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener,
    GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {

    companion object {
        const val GpsEnableRequestCode: Int = 111
    }

    val firestoreDatabase: FirebaseFirestore = Firebase.firestore

    val firebaseUser: FirebaseUser = Firebase.auth.currentUser!!

    val mapsLiveData: MapsLiveData by lazy {
        ViewModelProvider(this@MapsOfSociety).get(MapsLiveData::class.java)
    }

    val locationCheckpoint: LocationCheckpoint = LocationCheckpoint()

    val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this@MapsOfSociety)
    }

    lateinit var readyGoogleMap: GoogleMap

    val mapView: SupportMapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
    }

    val locationManager: LocationManager by lazy {
        applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    lateinit var userMapMarker: Marker

    val mapsMarker: MapsMarker by lazy {
        MapsMarker(this@MapsOfSociety, firebaseUser, readyGoogleMap, userMapMarker)
    }

    val locationCoordinatesUpdater: LocationCoordinatesUpdater by lazy {
        LocationCoordinatesUpdater(applicationContext, mapsLiveData)
    }

    var userLatitudeLongitude: LatLng? = null
    var nameOfCountry: String? = null

    val vicinityCalculations: VicinityCalculations = VicinityCalculations()

    val vicinityInformation: VicinityInformation by lazy {
        VicinityInformation(applicationContext)
    }

    val createVicinity: CreateVicinity by lazy {
        CreateVicinity(applicationContext, readyGoogleMap, firestoreDatabase)
    }

    val joinVicinity: JoinVicinity by lazy {
        JoinVicinity(applicationContext, readyGoogleMap, firestoreDatabase)
    }

    val userInformationIO: UserInformationIO by lazy {
        UserInformationIO(applicationContext)
    }

    val onlineOffline: OnlineOffline by lazy {
        OnlineOffline(Firebase.firestore)
    }

    val deviceSystemInformation: DeviceSystemInformation by lazy {
        DeviceSystemInformation(applicationContext)
    }

    val overallTheme: OverallTheme by lazy {
        OverallTheme(applicationContext)
    }

    var googleMapIsReady: Boolean = false

    @Inject lateinit var networkCheckpoint: NetworkCheckpoint

    @Inject lateinit var networkConnectionListener: NetworkConnectionListener

    lateinit var mapsViewBinding: MapsViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsViewBinding = MapsViewBinding.inflate(layoutInflater)
        setContentView(mapsViewBinding.root)

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
            .create(this@MapsOfSociety, mapsViewBinding.rootView)
            .inject(this@MapsOfSociety)

        mapsOfSocietySetupUI()

        networkConnectionListener.networkConnectionListenerInterface = this@MapsOfSociety

        val builderStrictMode = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builderStrictMode.build())

        mapsLiveData.currentLocationData.observe(this@MapsOfSociety, { location ->

            location?.let {
                Log.d(this@MapsOfSociety.javaClass.simpleName, "Location Updated ${location}")

                userLatitudeLongitude = location

                userInformationIO.saveUserLocation(location)

                mapsMarker.updateUserMarkerLocation(it)

                getLocationDetails()

                if (nameOfCountry != null && PublicCommunicationEndpoint.CurrentCommunityCoordinates != null) {

                    mapsLiveData.calculateVicinityCoordinates(location, PublicCommunicationEndpoint.CurrentCommunityCoordinates!!)

                    val vicinityUserInformation = VicinityUserInformation(firestoreDatabase,
                        PublicCommunicationEndpoint.publicCommunityDocumentEndpoint(nameOfCountry!!, PublicCommunicationEndpoint.CurrentCommunityCoordinates!!))

                    vicinityUserInformation.updateLocation(firebaseUser.uid,
                        location.latitude.toString(), location.longitude.toString(),
                        vicinityName(PublicCommunicationEndpoint.CurrentCommunityCoordinates!!), PublicCommunicationEndpoint.CurrentCommunityCoordinates!!)

                }

            }

        })

        mapsLiveData.vicinityNotice.observe(this@MapsOfSociety, {

            it?.let { vicinityNotice ->

                if (vicinityNotice.enteredNewVicinity) {

                    SnackbarBuilder(applicationContext).show (
                        rootView = mapsViewBinding.rootView,
                        messageText= getString(R.string.enteredNewVicinity),
                        messageDuration = Snackbar.LENGTH_INDEFINITE,
                        actionButtonText = R.string.joinText,
                        autoDismiss = true,
                        autoDismissDuration = 5555,
                        snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                            override fun onActionButtonClicked(snackbar: Snackbar) {
                                super.onActionButtonClicked(snackbar)

                                val countryIso = deviceSystemInformation.getCountryIso()

                                if (countryIso != "Undefined") {

                                    vicinityInformation.getCurrentCountryName(deviceSystemInformation.getCountryIso(), object : CountryInformationInterface {

                                        override fun countryNameReady(nameOfCountry: String) {

                                            this@MapsOfSociety.nameOfCountry = nameOfCountry

                                            loadVicinityData(nameOfCountry, vicinityNotice.userCurrentLocation)

                                        }

                                    })

                                }

                            }

                        }
                    )

                }

            }

        })

        mapsViewBinding.communicationHistory.setOnClickListener {

            startActivity(Intent(applicationContext, HistoryLists::class.java))

        }

        /*Invoke In Applicatio Update*/
        InApplicationUpdateProcess(this@MapsOfSociety, mapsViewBinding.rootView)
            .initialize()

    }

    override fun onStart() {
        super.onStart()

        if (nameOfCountry != null && PublicCommunicationEndpoint.CurrentCommunityCoordinates != null) {

            onlineOffline.startUserStateProcess(
                PublicCommunicationEndpoint.publicCommunityDocumentEndpoint(nameOfCountry!!, PublicCommunicationEndpoint.CurrentCommunityCoordinates!!),
                firebaseUser.uid,
                true
            )

        }

    }

    override fun onResume() {
        super.onResume()

        if (!mapsViewBinding.communicationHistory.isShown) {

            firestoreDatabase
                .collection(CommunicationHistoryEndpoint.publicVicinityArchiveDatabasePath(firebaseUser.uid))
                .get()
                .addOnSuccessListener {

                    if (!it.isEmpty) {

                        if (!mapsViewBinding.communicationHistory.isShown) {
                            mapsViewBinding.communicationHistory.visibility = View.VISIBLE
                        }

                    }

                }

            firestoreDatabase
                .collection(CommunicationHistoryEndpoint.privateVicinityArchiveDatabasePath(firebaseUser.uid))
                .get()
                .addOnSuccessListener {

                    if (!it.isEmpty) {

                        if (!mapsViewBinding.communicationHistory.isShown) {
                            mapsViewBinding.communicationHistory.visibility = View.VISIBLE
                        }

                    }

                }

        }

    }

    override fun onPause() {
        super.onPause()

        if (nameOfCountry != null && PublicCommunicationEndpoint.CurrentCommunityCoordinates != null) {

            onlineOffline.startUserStateProcess(
                PublicCommunicationEndpoint.publicCommunityDocumentEndpoint(nameOfCountry!!, PublicCommunicationEndpoint.CurrentCommunityCoordinates!!),
                firebaseUser.uid,
                false
            )

        }

    }

    override fun onDestroy() {
        super.onDestroy()

        networkConnectionListener.unregisterDefaultNetworkCallback()

    }

    override fun onBackPressed() {

        startActivity(Intent(Intent.ACTION_MAIN).apply {
            this.addCategory(Intent.CATEGORY_HOME)
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())

    }

    override fun enterPictureInPictureMode(pictureInPictureParams: PictureInPictureParams): Boolean {



        return super.enterPictureInPictureMode(pictureInPictureParams)
    }

    override fun networkAvailable() {

        if (networkCheckpoint.networkConnection() && locationCheckpoint.gpsAvailable(applicationContext)) {


            getLocationData()

        } else {

            locationCheckpoint.turnOnGps(this@MapsOfSociety)

        }

    }

    override fun networkLost() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            MapsOfSociety.GpsEnableRequestCode -> {

                if (networkCheckpoint.networkConnection() && locationCheckpoint.gpsAvailable(applicationContext)) {

                    getLocationData()

                } else {

                    Toast.makeText(applicationContext, getString(R.string.gpsIsOff), Toast.LENGTH_LONG).show()

                    SnackbarBuilder(applicationContext).show (
                        rootView = mapsViewBinding.rootView,
                        messageText= getString(R.string.selectLocationManually),
                        messageDuration = Snackbar.LENGTH_INDEFINITE,
                        actionButtonText = android.R.string.ok,
                        snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                            override fun onActionButtonClicked(snackbar: Snackbar) {
                                super.onActionButtonClicked(snackbar)

                                mapView.getMapAsync(this@MapsOfSociety)

                            }

                        }
                    )

                }

            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        readyGoogleMap = googleMap

        setupGoogleMap()

        userLatitudeLongitude?.let {

            readyGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 13.77f))

            addInitialMarker()

            val countryIso = deviceSystemInformation.getCountryIso()

            if (countryIso != "Undefined") {

                vicinityInformation.getCurrentCountryName(deviceSystemInformation.getCountryIso(), object : CountryInformationInterface {

                    override fun countryNameReady(nameOfCountry: String) {

                        this@MapsOfSociety.nameOfCountry = nameOfCountry

                        userLatitudeLongitude?.let { userLatitudeLongitude ->

                            loadVicinityData(nameOfCountry, userLatitudeLongitude)

                        }

                        mapsViewBinding.showPeopleView.visibility = View.VISIBLE

                    }

                })

            } else {

                SnackbarBuilder(applicationContext).show (
                    rootView = mapsViewBinding.rootView,
                    messageText= getString(R.string.undefinedCountry),
                    messageDuration = Snackbar.LENGTH_INDEFINITE,
                    actionButtonText = R.string.retryText,
                    snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                        override fun onActionButtonClicked(snackbar: Snackbar) {
                            super.onActionButtonClicked(snackbar)

                            startActivity(Intent(applicationContext, EntryConfiguration::class.java))

                            this@MapsOfSociety.finish()

                        }

                    }
                )

            }

            readyGoogleMap.setOnCircleClickListener {

                PublicCommunicationEndpoint.CurrentCommunityCoordinates?.let { currentCommunityCoordinates ->

                    nameOfCountry?.let { nameOfCountry ->

                        PublicCommunity.open(
                            context = applicationContext,
                            currentCommunityCoordinates = currentCommunityCoordinates,
                            nameOfCountry = nameOfCountry,
                        )

                    }

                }

            }

        }

    }

    override fun onMarkerClick(markerClick: Marker?): Boolean {

        markerClick?.let {

            val markerLocation = markerClick.position

            val projection: Projection = readyGoogleMap.projection
            val screenPosition = projection.toScreenLocation(markerLocation)

            val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LatLng(markerClick.position.latitude + VicinityCalculations.MarkerClickCameraOffset, markerClick.position.longitude), 19.7f)
            readyGoogleMap.animateCamera(cameraUpdateFactory)

            val informationWindow: InformationWindow = InformationWindow(this@MapsOfSociety)

            val informationWindowRootView = informationWindow.getRootView()

            mapsViewBinding.informationWindowContainer.addView(informationWindowRootView.root)

            mapsViewBinding.informationWindowContainer.visibility = View.VISIBLE

            firestoreDatabase
                .document(UserInformation.userProfileDatabasePath(markerClick.tag as String))
                .get()
                .addOnSuccessListener {
                    Log.d(this@MapsOfSociety.javaClass.simpleName, "${it.data}")

                    val informationWindowData = InformationWindowData(
                        userDocument = it
                    )

                    informationWindow.informationWindowData = informationWindowData

                    informationWindow.setUpContentContents(markerClick)

                    informationWindowRootView.profileLoadingView.visibility = View.INVISIBLE

                }.addOnFailureListener {

                    mapsViewBinding.informationWindowContainer.visibility = View.VISIBLE

                    mapsViewBinding.informationWindowContainer.removeAllViews()

                }

            Log.d(this@MapsOfSociety.javaClass.simpleName, "Location: ${markerLocation} - Screen Position: ${screenPosition}")

        }

        return true
    }

    override fun onMapLongClick(latLng: LatLng?) {
        Log.d(this@MapsOfSociety.javaClass.simpleName, "${latLng}")

        latLng?.let {

            userLatitudeLongitude = latLng

            mapView.getMapAsync(this@MapsOfSociety)

        }

    }

    override fun onMapClick(latLng: LatLng?) {

    }

    override fun onCameraMove() {

    }

    override fun onCameraIdle() {

    }

}