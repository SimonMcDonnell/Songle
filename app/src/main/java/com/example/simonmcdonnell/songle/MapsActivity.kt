package com.example.simonmcdonnell.songle

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.*
import android.widget.TextView
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
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.guess_song.*
import kotlinx.android.synthetic.main.hints.*
import kotlinx.android.synthetic.main.list_layout.*
import java.nio.charset.StandardCharsets

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private val TAG = "LOG_TAG"
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient : GoogleApiClient
    private lateinit var mLastLocation : Location
    private lateinit var lyricList: List<List<String>>
    private lateinit var collectedLyrics: ArrayList<String>
    private lateinit var kmlString: String
    private lateinit var layer: KmlLayer
    private lateinit var extras: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Get extras from intent and build url
        extras = intent.extras
        val song_lyrics = extras["LYRICS"] as String
        val lyric_lines = song_lyrics.split("\n")
        lyricList = lyric_lines.map { it.trim().split(" ") }
        kmlString = extras["KML"] as String
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
        val fab_item3 = fab_menu_item3
        // Set up menu behaviour, allowing FAB to move for snackbars at bottom of screen
        val layoutParams = fab_main.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = FloatingActionMenuBehavior()
        fab_main.requestLayout()
        collectedLyrics = ArrayList()
        collectedLyrics.add("eyes")
        collectedLyrics.add("sympathy")
        collectedLyrics.add("begun")
        collectedLyrics.add("Mama")
        collectedLyrics.add("Galileo")
        collectedLyrics.add("Any way the wind blows doesn't really matter to me, to me")
        collectedLyrics.add("poor")
        collectedLyrics.add("boy")
        collectedLyrics.add("killed")
        collectedLyrics.add("shivers")
        collectedLyrics.add("Thunderbolt")
        collectedLyrics.add("Scaramouche")
        collectedLyrics.add("Fandango")
        collectedLyrics.add("Magnifico-o-o-o-o")
        collectedLyrics.add("Beelzebub")
        collectedLyrics.add("baby")
        collectedLyrics.add("nobody")
        collectedLyrics.add("frightening")
        collectedLyrics.add("wind")
        fab_item1.setOnClickListener { _ ->
            fab_main.close(true)
            viewCollectedLyrics()
        }
        fab_item2.setOnClickListener { _ ->
            fab_main.close(true)
            guessSong()
        }
        fab_item3.setOnClickListener { _ ->
            fab_main.close(true)
            showHints()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Get shared preferences
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val isTimed = settings.getBoolean("timer", false)
        if (isTimed) {
            menuInflater.inflate(R.menu.timer, menu)
            val timer = menu?.findItem(R.id.countdown_timer)
            val timerText = MenuItemCompat.getActionView(timer) as TextView
            timerText.setPadding(20, 0, 50, 0)
            timerText.textSize = 24f
            when (settings.getString("difficulty", "3")) {
                "5" -> startTimer(timerText, 1800000, 1000)
                "4" -> startTimer(timerText, 1800000, 1000)
                "3" -> startTimer(timerText, 1200000, 1000)
                "2" -> startTimer(timerText, 900000, 1000)
                "1" -> startTimer(timerText, 600000, 1000)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    fun startTimer(timerText: TextView, duration: Long, interval: Long) {
        val countDownTimer = object: CountDownTimer(duration, interval) {
            override fun onFinish() {
                // When the timer runs out
                val alertDialog = AlertDialog.Builder(this@MapsActivity).create()
                alertDialog.setTitle("Time's up!")
                alertDialog.setMessage("You ran out of time, better walk faster!")
                alertDialog.setOnCancelListener { _ -> finish() }
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { _, _ -> finish()})
                alertDialog.show()
            }

            override fun onTick(milliSecondsRemaining: Long) {
                // Update the timer
                val secondsRemaining = Math.round(milliSecondsRemaining / 1000.0)
                timerText.text = secondsToString(secondsRemaining.toInt())
                timerText.setTextColor(Color.WHITE)
            }
        }
        countDownTimer.start()
    }

    fun secondsToString(secondsRemaining: Int): String {
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val minuteString = if (minutes < 10) "0$minutes" else minutes.toString()
        val secondString = if (seconds < 10) "0$seconds" else seconds.toString()
        return minuteString + ":" + secondString
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
        lyricList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        lyricList.adapter = CollectedLyricsAdapter(this, collectedLyrics)
        dialog.window.attributes.windowAnimations = R.style.dialog_animation
        dialog.show()
        dialog.window.attributes = layoutParams
    }

    fun guessSong() {
        Snackbar.make(maps_activity_layout, "New lyric - Fandango", Snackbar.LENGTH_LONG).show()
        // Build dialog and set dimensions
        val dialog = Dialog(this)
//        val layoutParams = WindowManager.LayoutParams()
//        layoutParams.copyFrom(dialog.window.attributes)
//        val metrics = resources.displayMetrics
//        val screenHeight = metrics.heightPixels / 3
//        layoutParams.height = screenHeight
        dialog.setContentView(R.layout.guess_song)
        // Set on click listener for guess button
        dialog.guess_song_button.setOnClickListener { _ ->
            val guess = dialog.guess_song_input.text.toString().toLowerCase().trim()
            val song_title = extras["NAME"] as String
            if (guess == song_title.toLowerCase()) {
                // If guess is correct take the user to GuessedActivity
                val guessedIntent = Intent(this, GuessedActivity::class.java)
                guessedIntent.putExtra("NAME", song_title)
                guessedIntent.putExtra("ARTIST", extras["ARTIST"] as String)
                guessedIntent.putExtra("LINK", extras["LINK"] as String)
                guessedIntent.putExtra("LYRICS", extras["LYRICS"] as String)
                startActivity(guessedIntent)
                gameOver(1)
            } else {
                dialog.dismiss()
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle("Nope")
                alertDialog.setMessage("That's not right, keep trying!")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialog, _ -> dialog.dismiss()})
                alertDialog.show()
            }
        }
        dialog.show()
//        dialog.window.attributes = layoutParams
    }

    fun showHints() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.hints)
        dialog.hint_XP.text = "You have 3000XP"
        dialog.show()
//        val alertDialog = AlertDialog.Builder(this).create()
//        alertDialog.setTitle("Nope")
//        alertDialog.setMessage("Sorry, you don't have enough XP")
//        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialog, _ -> dialog.dismiss()})
//        alertDialog.show()
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
        // Add Kml layer to the map
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

    override fun onBackPressed() {
        val listener = DialogInterface.OnClickListener { _, i ->
            if (i == DialogInterface.BUTTON_POSITIVE) {
                gameOver(0)
            }
        }
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Are you sure you want to quit?")
        dialog.setPositiveButton("Quit", listener)
        dialog.setNegativeButton("Cancel", listener)
        dialog.show()
    }

    fun gameOver(result: Int) {
        // Finish the activity with result of SUCCESS or FAILURE
        val gameOverIntent = Intent()
        gameOverIntent.putExtra("NAME", extras["NAME"] as String)
        gameOverIntent.putExtra("ARTIST", extras["ARTIST"] as String)
        gameOverIntent.putExtra("LINK", extras["LINK"] as String)
        setResult(result, gameOverIntent)
        finish()
    }

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
