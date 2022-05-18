package com.example.gpstracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


const val LOCATION_KEY = "location" // Key for extras passed into MapActivity.kt
// Default location if location permission is denied.
private const val DEFAULT_LOCATION_PROVIDER = "default"
private const val DEFAULT_LATITUDE = 45.0
private const val DEFAULT_LONGITUDE = 45.0

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var btnNext : Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnNext = findViewById(R.id.btnNext)

        btnNext.setOnClickListener {
            requestPermission()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    /** The request permission launcher is used to prompt the user to make their
     * choice on allowing permission. The @suppressLint exists because the
     * fusedLocationClient does not have its expected permissions added. This
     * is handled by the (isGranted) check, because the program will never call fusedLocationClient
     * w/o checking if location permissions have been granted
     */

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Find current location and launch map
                fusedLocationClient.lastLocation.addOnCompleteListener {location ->
                    when (location.result){
                        null -> {
                            Toast.makeText(this, "Location not Found", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            startMap(location.result)
                        }
                    }
                }
            } else {
                // Permission is denied and map will start with default coordinates
                Log.i(TAG, "requestPermissionLauncher: Permission Denied")
                val defaultLoc = Location(DEFAULT_LOCATION_PROVIDER)
                defaultLoc.latitude = DEFAULT_LATITUDE
                defaultLoc.longitude = DEFAULT_LONGITUDE
                startMap(defaultLoc)
            }
        }

    /**
     * Checks whether permission has been granted and proceeds accordingly
     */
    private fun requestPermission() {
        when{
            // Check if permission has been granted, If it has, launch map activity
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "requestPermission: Access granted, find current loc and launch")
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            // Check if the user needs to see additional dialog providing rationale for request
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) ->{
                dialogUI()
                Log.i(TAG, "requestPermission: User needs rationale")
            }
            // If rational is not needed, ask the user for permission
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                Log.i(TAG, "requestPermission: User has not been asked, asking now")
            }
        }
    }

    /**
     * Dialog designed to give user extra information on why Location is being used.
     */
    private fun dialogUI() {
        val infoView = LayoutInflater.from(this).inflate(R.layout.infoview, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Location Permissions")
            .setView(infoView)
            .setPositiveButton("OK", null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            dialog.dismiss()
            Log.i(TAG,"dialogUI: requestPermissionLauncher.launch" )
            requestPermissionLauncher.launch(
            Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    private fun startMap(location: Location){
        // Will navigate to map activity either with default coordinates, or current coordinates.
        Log.i(TAG, "StartMap: ${location.latitude}, ${location.longitude}")
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra(LOCATION_KEY, location)
        startActivity(intent)
    }
}