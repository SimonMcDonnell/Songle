package com.example.simonmcdonnell.songle

import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient : GoogleApiClient
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    var mLocationPermissionGranted = false
    private lateinit var mLastLocation : Location
    val TAG = "MapsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        // Get notified when map is ready to use
        mapFragment.getMapAsync(this)
        // Create instance of GoogleApiClient
        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
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
    }

    override fun onConnected(connectionHint: Bundle?) {
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
            println("[onLocationChanged] Lat/Long now (${current.getLatitude()}, ${current.getLongitude()})")
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
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, myLocationRequest, this)
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect()
        }
    }
}
