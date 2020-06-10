package edu.ohiostate.whereamikotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Class for Maps Fragment. Sources:
 * - Big Nerd Ranch Guide to Android Programming, Chap. 34
 * - Google: https://developers.google.com/maps/documentation/android-api/current-place-tutorial
 *
 *
 * Created by adamcchampion on 2017/09/24.
 */

class MapsFragment : SupportMapFragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap // Might be null if Google Play services APK is not available.
    private lateinit var mApiClient: GoogleApiClient
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLocation: Location? = null
    private var mDefaultLocation: LatLng? = null
    private var mLocationPermissionGranted = false
    private var mMapReady = false

    private val TAG = javaClass.simpleName
    private var mSettings: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        mApiClient = GoogleApiClient.Builder(requireActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {
                        activity?.invalidateOptionsMenu()
                    }

                    override fun onConnectionSuspended(i: Int) {
                        Log.d(TAG, "GoogleAPIClient connection suspended")
                    }
                })
                .build()
        getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        setUpEula()
        findLocation()
    }

    @SuppressLint("MissingPermission")
    private fun findLocation() {
        updateLocationUI()
        if (hasLocationPermission()) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            mDefaultLocation = LatLng(40.0, -83.0)
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.numUpdates = 1
            locationRequest.interval = 0
            val locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
            val locationResult = locationProvider.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    mLocation = task.result as Location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(mLocation!!.latitude, mLocation!!.longitude), 16f))
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 16f))
                    mMap.uiSettings?.isMyLocationButtonEnabled = false
                }
            }
        } else {
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS)
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.invalidateOptionsMenu()
        mApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        mApiClient.disconnect()
    }

    private fun setUpEula() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val isEulaAccepted = mSettings!!.getBoolean(getString(R.string.eula_accepted_key), false)
        if (!isEulaAccepted) {
            val eulaDialogFragment = EulaDialogFragment()
            if (activity != null) {
                eulaDialogFragment.show(requireActivity().supportFragmentManager, "eula")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.maps_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_showcurrentlocation -> {
                Log.d(TAG, "Showing current location")
                if (hasLocationPermission()) {
                    findLocation()
                } else {
                    requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS)
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            REQUEST_LOCATION_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (mMapReady) {
            try {
                if (mLocationPermissionGranted) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                } else {
                    mMap.isMyLocationEnabled = false
                    mMap.uiSettings.isMyLocationButtonEnabled = false
                    mLocation = null
                    requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS)
                }
            } catch (e: SecurityException) {
				val msg = e.message
				if (msg != null) {
					Log.e("Exception: %s", msg)
				}
            }
        }
        else {
            Log.d(TAG, "Map not ready, skipping")
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(LatLng(40.0, -83.0))
                .title("Ohio State University"))
        try {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        } catch (se: SecurityException) {
            Log.e(TAG, "Location not enabled, skipping")
        }
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true
        mMapReady = true
    }

    private fun hasLocationPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        private const val REQUEST_LOCATION_PERMISSIONS = 0
    }
}
