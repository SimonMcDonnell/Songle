package com.example.simonmcdonnell.songle

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.WindowManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlPoint
import java.io.ByteArrayInputStream
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.guess_song.*
import kotlinx.android.synthetic.main.list_layout.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private val TAG = "LOG_TAG"
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient : GoogleApiClient
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var mLastLocation : Location
    private lateinit var url: String
    private lateinit var lyric_list: List<List<String>>
    private lateinit var collectedLyrics: ArrayList<String>
    private lateinit var kmlString: String
    private lateinit var layer: KmlLayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Get extras from intent and build url
        val extras = intent.extras
        val map_id = extras["ID"]
        val song_name = extras["NAME"]
        val song_artist = extras["ARTIST"]
        val song_link = extras["LINK"]
        val song_lyrics = extras["LYRICS"] as String
        val lyric_lines = song_lyrics.split("\n")
        lyric_list = lyric_lines.map { it.trim().split(" ") }
        kmlString = extras["KML"] as String
//        url = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/$map_id/map1.kml"
//        Log.v(TAG, url)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        // Get notified when map is ready to use
        mapFragment.getMapAsync(this)
        // Create instance of GoogleApiClient
        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
        // Set up Floating action button menu
        val fab_main = fab_main
        val fab_item1 = fab_menu_item1
        val fab_item2 = fab_menu_item2
        collectedLyrics = ArrayList()
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        collectedLyrics.add("Hello")
        collectedLyrics.add("bye")
        fab_item1.setOnClickListener { _ ->
            fab_main.close(true)
            viewCollectedLyrics()
        }
        fab_item2.setOnClickListener { _ ->
            fab_main.close(true)
            guessSong()
        }
    }

    fun collectLyric(lyricID: String) {
        Log.v(TAG, "Collected $lyricID")
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("You've collected a lyric")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialog, _ -> dialog.dismiss()})
        alertDialog.show()
    }

    fun viewCollectedLyrics() {
        // Display custom view of lyrics collected
        val dialog = Dialog(this)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.setContentView(R.layout.list_layout)
        val lyricList = dialog.recyclerview
        lyricList.layoutManager = LinearLayoutManager(this)
        lyricList.adapter = SongListAdapter(this, collectedLyrics)
        dialog.show()
        dialog.window.attributes = layoutParams
    }

    fun guessSong() {
        val dialog = Dialog(this)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window.attributes)
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.setContentView(R.layout.guess_song)
        val guess_button = dialog.guess_song_button
        dialog.show()
        dialog.window.attributes = layoutParams
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Move camera to game region in Edinburgh
        val edinburgh = LatLngBounds(LatLng(55.942617, -3.192473),
                LatLng(55.946233, -3.184319))
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(edinburgh, 900, 900, 0))
        // Visualise current position with small blue circle
        try {
            mMap.isMyLocationEnabled = true
        } catch (se : SecurityException) {
            println("Security Exception thrown [onMapReady]")
        }
        // Add "My Location" button to screen
        mMap.uiSettings.isMyLocationButtonEnabled = true
//        DownloadKMLTask(this).execute(url)
        layer = KmlLayer(mMap, kmlString.byteInputStream(StandardCharsets.UTF_8), this)
        layer.addLayerToMap()
    }

    override fun onConnected(connectionHint: Bundle?) {
        Log.v(TAG, "Connected")
        try {
            createLocationRequest()
        } catch (ise: IllegalStateException) {
            println("Illegal state exception thrown [onConnected]")
        }
        // Can we access user's location
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onConnectionSuspended(flag: Int) {
        println(">>>>onConnectionSuspended")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        // An unresolvable error has occured, connection to GoogleAPIs could not be established
        println(">>>>onConnectionFailed")
    }

    override fun onLocationChanged(current: Location?) {
        if (current == null) {
            println("[onLocationUnchanged] Location unknown")
        } else {
            println("[onLocationChanged] Lat/Long now (${current.latitude}, ${current.longitude})")
            for (container in layer.containers) {
                for (placemark in container.placemarks) {
                    val point = placemark.geometry as KmlPoint
                    val point_lat = point.geometryObject.latitude
                    val point_long = point.geometryObject.longitude
                    val lat_dist = Math.abs(current.latitude - point_lat)
                    val long_dist = Math.abs(current.longitude - point_long)
                    val dist = Math.sqrt(Math.pow(lat_dist, 2.0) + Math.pow(long_dist, 2.0))
                    if (dist <= 0.00015) {
                        collectLyric("Got one!")
                        break
                    }
                }
            }
        }
        // Update location
        createLocationRequest()
    }

    fun createLocationRequest() {
        // Set parameters for location request
        val myLocationRequest = LocationRequest()
        myLocationRequest.interval = 5000 // 5 seconds
        myLocationRequest.fastestInterval = 1000
        myLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        // Check if we can access the user's current location
        val permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED && mGoogleApiClient.isConnected) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, myLocationRequest, this)
        }
    }

//    override fun downloadComplete(byteArr: ByteArray) {
//        // Add the word markers to the map
//        layer = KmlLayer(mMap, ByteArrayInputStream(byteArr), this)
//        layer.addLayerToMap()
//        // Add listener for when
//        layer.setOnFeatureClickListener { View ->
//            Log.v(TAG, View.getProperty("name"))
//        }
//    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }
}
