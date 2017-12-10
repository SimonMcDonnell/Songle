package com.example.simonmcdonnell.songle

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlPoint
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.guess_song.*
import kotlinx.android.synthetic.main.hints.*
import kotlinx.android.synthetic.main.list_layout.*
import java.nio.charset.StandardCharsets
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private val TAG = "LOG_TAG"
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient : GoogleApiClient
    private lateinit var lyricList: List<List<String>>
    private lateinit var collectedLyrics: ArrayList<String>
    private lateinit var kmlString: String
    private lateinit var markers: ArrayList<Marker>
    private lateinit var extras: Bundle
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Get extras from intent and initialize global variables
        extras = intent.extras
        kmlString = extras["KML"] as String
        val songLyrics = extras["LYRICS"] as String
        markers = ArrayList()
        collectedLyrics = ArrayList()
        // Split lyrics at the newline and then turn each line into list of words
        val lyricLines = songLyrics.split("\n")
        lyricList = lyricLines.map { it.trim().split(" ") }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        // Get notified when map is ready to use
        mapFragment.getMapAsync(this)
        // Create instance of GoogleApiClient
        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
        // Set up Floating action button menu
        val fabMain = fab_main
        val fabItem1 = fab_menu_item1
        val fabItem2 = fab_menu_item2
        val fabItem3 = fab_menu_item3
        // Set up menu behaviour, allowing FAB to move for snackbars at bottom of screen
        val layoutParams = fab_main.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = FloatingActionMenuBehavior()
        fabMain.requestLayout()
        // Set up onClick behaviour of FAB buttons
        fabItem1.setOnClickListener { _ ->
            fab_main.close(true)
            viewCollectedLyrics()
        }
        fabItem2.setOnClickListener { _ ->
            fab_main.close(true)
            guessSong()
        }
        fabItem3.setOnClickListener { _ ->
            fab_main.close(true)
            showHints()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Get shared preferences
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val isTimed = settings.getBoolean("timer", false)
        Log.v(TAG, "OnCreateOptionsMenu isTimed=$isTimed")
        // If timed mode is on then add timer to menu
        if (isTimed) {
            menuInflater.inflate(R.menu.timer, menu)
            val timer = menu?.findItem(R.id.countdown_timer)
            val timerText = MenuItemCompat.getActionView(timer) as TextView
            timerText.setPadding(20, 0, 50, 0)
            timerText.textSize = 24f
            when (settings.getString("difficulty", "3")) {
                // Start timer for different durations depending on difficulty
                "5" -> startTimer(timerText, 1800000, 1000)
                "4" -> startTimer(timerText, 1800000, 1000)
                "3" -> startTimer(timerText, 1200000, 1000)
                "2" -> startTimer(timerText, 900000, 1000)
                "1" -> startTimer(timerText, 600000, 1000)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun startTimer(timerText: TextView, duration: Long, interval: Long) {
        countDownTimer = object: CountDownTimer(duration, interval) {
            override fun onFinish() {
                // When the timer runs out show dialog
                val alertDialog = AlertDialog.Builder(this@MapsActivity).create()
                alertDialog.setTitle("Time's up!")
                alertDialog.setMessage("You ran out of time, better walk faster!")
                alertDialog.setOnCancelListener { _ -> gameOver(0) }
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { _, _ -> gameOver(0)})
                alertDialog.show()
            }

            override fun onTick(milliSecondsRemaining: Long) {
                // Update the timer every second
                val secondsRemaining = Math.round(milliSecondsRemaining / 1000.0)
                timerText.text = secondsToString(secondsRemaining.toInt())
                timerText.setTextColor(Color.WHITE)
            }
        }
        countDownTimer.start()
    }

    fun secondsToString(secondsRemaining: Int): String {
        // Format the timer string
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val minuteString = if (minutes < 10) "0$minutes" else minutes.toString()
        val secondString = if (seconds < 10) "0$seconds" else seconds.toString()
        return minuteString + ":" + secondString
    }

    private fun collectLyric(lyricID: String, showLyric: Boolean = true) {
        // Get the location of lyric from lyricID
        val location = lyricID.split(":").map { it.toInt() }
        // Return lyric at location and remove specfic trailing characters
        val lyric = lyricList[location[0] - 1][location[1] - 1].trim(',', ')', '.')
        if (showLyric) Snackbar.make(maps_activity_layout, "New lyric - $lyric", Snackbar.LENGTH_LONG).show()
        collectedLyrics.add(0, lyric)
    }

    private fun viewCollectedLyrics() {
        // Display lyrics collected
        val dialog = Dialog(this, R.style.AppTheme_OnlyActionBar)
        dialog.setContentView(R.layout.list_layout)
        val lyricList = dialog.recyclerview
        // Display as staggered grid
        lyricList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        lyricList.adapter = CollectedLyricsAdapter(collectedLyrics)
        // Have dialog enter with defined animation
        dialog.window.attributes.windowAnimations = R.style.dialog_animation
        dialog.show()
    }

    private fun guessSong() {
        val dialog = Dialog(this, R.style.AppTheme_OnlyActionBar)
        dialog.setContentView(R.layout.guess_song)
        // Set on click listener for guess button
        dialog.guess_song_button.setOnClickListener { _ ->
            // Remove whitespace from guess and make lowercase for comparison
            val guess = dialog.guess_song_input.text.toString().toLowerCase().trim()
            val songTitle = extras["NAME"] as String
            if (guess == songTitle.toLowerCase()) {
                // If guess is correct take the user to SuccessActivity, passing the song data
                val guessedIntent = Intent(this, SuccessActivity::class.java)
                guessedIntent.putExtra("NAME", songTitle)
                guessedIntent.putExtra("ARTIST", extras["ARTIST"] as String)
                guessedIntent.putExtra("LINK", extras["LINK"] as String)
                guessedIntent.putExtra("LYRICS", extras["LYRICS"] as String)
                startActivity(guessedIntent)
                // Indicate song was completed by passing success value to gameOver
                gameOver(1)
            } else {
                dialog.dismiss()
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle("Nope")
                alertDialog.setMessage("That's not right, keep trying!")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { d, _ -> d.dismiss()})
                alertDialog.show()
            }
        }
        dialog.window.attributes.windowAnimations = R.style.dialog_animation
        dialog.show()
    }

    private fun showHints() {
        val dialog = Dialog(this, R.style.AppTheme_OnlyActionBar)
        dialog.setContentView(R.layout.hints)
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val xp = settings.getInt("XP", 0)
        dialog.hint_XP.text = "You have ${xp}XP"
        // Set on click listener for random lyric button
        dialog.hint_random.setOnClickListener { _ ->
            val listener = DialogInterface.OnClickListener { _, i ->
                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                // If the user selects YES, has enough xp and there are enough markers remaining
                if (i == DialogInterface.BUTTON_POSITIVE && xp >= 30 && markers.size >= 3) {
                    // Update user's total XP
                    val newXP = xp - 30
                    editor.putInt("XP", newXP)
                    editor.apply()
                    // add 3 random lyrics to collectedLyrics
                    val rand = Random()
                    for (i in 0..2) {
                        val index = rand.nextInt(markers.size)
                        val marker = markers[index]
                        collectLyric(marker.title, false)
                        marker.remove()
                        markers.removeAt(index)
                    }
                    dialog.dismiss()
                    // Display confirmation dialog
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle("New Lyrics!")
                    alertDialog.setMessage("You have unlocked new lyrics. You now have ${newXP}XP")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { d, _ -> d.dismiss() })
                    alertDialog.show()
                } else if (i == DialogInterface.BUTTON_POSITIVE && xp >= 30) {
                    // If there are not enough markers remaining, prevent user from spending XP
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle("Nope")
                    alertDialog.setMessage("Sorry, there are not enough lyrics remaining")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { d, _ -> d.dismiss() })
                    alertDialog.show()
                } else if (i == DialogInterface.BUTTON_POSITIVE) {
                    // If the user doesn't have enough XP then display error message
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle("Nope")
                    alertDialog.setMessage("Sorry, you don't have enough XP")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { d, _ -> d.dismiss() })
                    alertDialog.show()
                }
            }
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage("Get 3 random lyrics for 30XP?")
            alertDialog.setPositiveButton("Yes", listener)
            alertDialog.setNegativeButton("Cancel", listener)
            alertDialog.show()
        }
        // Set on click listener for whole line button
        dialog.hint_line.setOnClickListener { _ ->
            val listener = DialogInterface.OnClickListener { _, i ->
                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                // If the user selects YES and has enough xp
                if (i == DialogInterface.BUTTON_POSITIVE && xp >= 100) {
                    // Update user's total XP
                    val newXP = xp - 100
                    editor.putInt("XP", newXP)
                    editor.apply()
                    // add a whole line to collectedLyrics
                    // The loop ensures we aren't getting lines with zero or one words
                    var goodLine = false
                    val rand = Random()
                    while (!goodLine) {
                        val line = lyricList[rand.nextInt(lyricList.size)]
                        if (line.size > 1) {
                            collectedLyrics.add(0, line.joinToString(" "))
                            goodLine = true
                        }
                    }
                    dialog.dismiss()
                    // Display confirmation dialog
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle("New Lyrics!")
                    alertDialog.setMessage("You have unlocked new lyrics. You now have ${newXP}XP")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { d, _ -> d.dismiss() })
                    alertDialog.show()
                } else if (i == DialogInterface.BUTTON_POSITIVE) {
                    // If the user doesn't have enough XP then display error message
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle("Nope")
                    alertDialog.setMessage("Sorry, you don't have enough XP")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { d, _ -> d.dismiss() })
                    alertDialog.show()
                }
            }
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage("Get a line of the song for 100XP?")
            alertDialog.setPositiveButton("Yes", listener)
            alertDialog.setNegativeButton("Cancel", listener)
            alertDialog.show()
        }
        dialog.window.attributes.windowAnimations = R.style.dialog_animation
        dialog.show()
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
        addMapMarkers()
    }

    private fun addMapMarkers() {
        // Add Kml layer to the map
        Log.v(TAG, "added layer to map $kmlString")
        val layer = KmlLayer(mMap, kmlString.byteInputStream(StandardCharsets.UTF_8), this)
        layer.addLayerToMap()
        // Replace each placemark with a Map Marker. This is done so we can remove markers when collected.
        for (container in layer.containers) {
            for (placemark in container.placemarks) {
                val point = placemark.geometry as KmlPoint
                val title = placemark.getProperty("name")
                val description = placemark.getProperty("description")
                val iconBitmap = BitmapFactory.decodeResource(resources, getPlacemarkImage(description))
                val resizedIconBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false)
                val marker = mMap.addMarker(MarkerOptions()
                        .position(LatLng(point.geometryObject.latitude, point.geometryObject.longitude))
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizedIconBitmap)))
                markers.add(marker)
            }
        }
        // Remove duplicate layer from map
        layer.removeLayerFromMap()
        // Set the onClick event of a marker to do nothing
        mMap.setOnMarkerClickListener { _ -> true }
    }

    private fun getPlacemarkImage(description: String): Int {
        return when (description) {
            "veryinteresting" -> R.mipmap.veryinteresting
            "interesting" -> R.mipmap.interesting
            "notboring" -> R.mipmap.notboring
            "boring" -> R.mipmap.boring
            "unclassified" -> R.mipmap.unclassified
            else -> R.mipmap.unclassified
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        Log.v(TAG, "Connected")
        try {
            createLocationRequest()
        } catch (ise: IllegalStateException) {
            Log.v(TAG, "Illegal state exception thrown [onConnected]")
        }
        // Can we access user's location
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            Log.v(TAG, "mLastLocation $mLastLocation")
        } else {
            // Ask for permission to use location
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
            // Check to see if any markers are within 25m from our location
            for (i in markers.indices) {
                val marker = markers[i]
                val lat = marker.position.latitude
                val long = marker.position.longitude
                // Calculate difference in latitude and longitude in game region, converted to metres
                val latDiff = Math.abs(current.latitude - lat) * 111340.77
                val longDiff = Math.abs(current.longitude - long) * 62482.25
                val dist = Math.sqrt(Math.pow(latDiff, 2.0) + Math.pow(longDiff, 2.0))
                if (dist <= 25) {
                    // If within 25m of a marker, collect the lyric and remove it from map
                    collectLyric(marker.title)
                    marker.remove()
                    markers.removeAt(i)
                    break
                }
            }
        }
        // Update location
        createLocationRequest()
    }

    private fun createLocationRequest() {
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            // If the activity is destroyed, cancel the countDownTimer
            countDownTimer.cancel()
        } catch (exception: UninitializedPropertyAccessException) {}
        gameOver(0)
        Log.v(TAG, "Activity Destroyed")
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
