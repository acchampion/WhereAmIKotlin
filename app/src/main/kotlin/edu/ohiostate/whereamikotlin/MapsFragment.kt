package edu.ohiostate.whereamikotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

/**
 * Class for Maps Fragment. Sources:
 * - Big Nerd Ranch Guide to Android Programming, Chap. 34
 * - Google: https://developers.google.com/maps/documentation/android-api/current-place-tutorial
 *
 *
 * Created by adamcchampion on 2017/09/24.
 */
class MapsFragment : SupportMapFragment(), OnMapReadyCallback, OnMyLocationButtonClickListener,
	OnMyLocationClickListener {
    private lateinit var mMap: GoogleMap // Could be null if Google Play services APK is unavailable
    private var mLocation: Location? = null
    private var mDefaultLocation: LatLng? = null
	private var mMapReady = false
	private val classTag = javaClass.simpleName

    private val mActivityResult = registerForActivityResult(
        RequestPermission()
    ) { result: Boolean ->
        if (result) {
            // We have permission, so show the user's location.
            findLocation()
            updateLocationUI()
        } else {
            // The user denied location permission, so show them a message.
            Log.e(classTag, "Error: location permission denied")
            if (lacksLocationPermission()) {
                Toast.makeText(requireActivity(), "Location permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        setUpEula()
        findLocation()
    }

    @SuppressLint("MissingPermission")
    private fun findLocation() {
        val activity: Activity = requireActivity()
        mDefaultLocation = LatLng(40.0, -83.0)
        val locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.numUpdates = 1
        locationRequest.interval = 0
        val locationProvider = LocationServices.getFusedLocationProviderClient(activity)
        if (hasLocationPermission()) {
            updateLocationUI()
            val locationResult = locationProvider.lastLocation
            locationResult.addOnCompleteListener(activity) { task: Task<Location?> ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    mLocation = task.result
                    if (mLocation != null) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLocation!!.latitude,
                                    mLocation!!.longitude
                                ), 12f
                            )
                        )
                    }
                } else {
                    Log.d(classTag, "Current location is null. Using defaults.")
                    Log.e(classTag, "Exception: %s", task.exception)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation!!, 16f))
                    mMap.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val activity: Activity = requireActivity()
        activity.invalidateOptionsMenu()
    }

	private fun setUpEula() {
        val activity = requireActivity()
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val isEulaAccepted = sharedPrefs.getBoolean(getString(R.string.eula_accepted_key), false)
        if (!isEulaAccepted) {
            val eulaDialogFragment: DialogFragment = EulaDialogFragment()
            eulaDialogFragment.show(activity.supportFragmentManager, "eula")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.maps_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_showcurrentlocation) {
            Log.d(classTag, "Showing current location")
            if (lacksLocationPermission()) {
                mActivityResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                findLocation()
            }
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (hasLocationPermission() && mMapReady) {
			mMap.isMyLocationEnabled = true
			mMap.uiSettings.isMyLocationButtonEnabled = true
		}
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
		mMapReady = true
        mMap.addMarker(
            MarkerOptions().position(LatLng(40.0, -83.0))
                .title("Ohio State University")
        )
        if (hasLocationPermission()) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true
    }

    private fun lacksLocationPermission(): Boolean {
        val activity: Activity = requireActivity()
        val result =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        return result != PackageManager.PERMISSION_GRANTED
    }

    private fun hasLocationPermission(): Boolean {
        return !lacksLocationPermission()
    }

	override fun onMyLocationButtonClick(): Boolean {
		val context = requireContext()
		Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
		if (hasLocationPermission()) {
			findLocation()
		}
		// Return false so that we don't consume the event and the default behavior still occurs
		// (the camera animates to the user's current position).
		return false
	}

	override fun onMyLocationClick(location: Location) {
		val context = requireContext()
		Toast.makeText(context, "Current location:\n$location", Toast.LENGTH_LONG).show()
	}

}
