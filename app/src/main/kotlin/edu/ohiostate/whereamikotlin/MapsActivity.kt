package edu.ohiostate.whereamikotlin

import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class MapsActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment {
        return MapsFragment()
    }

    /**
     * onResume() checks if Google Play services is available. If not, the Activity shows an
     * error dialog. Code from Chap. 33, Big Nerd Ranch Guide to Android Programming, 3rd ed.
     */
    override fun onResume() {
        super.onResume()
        val apiAvailability = GoogleApiAvailability.getInstance()
        val errorCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (errorCode != ConnectionResult.SUCCESS) {
            val errorDialog = apiAvailability.getErrorDialog(
                this, errorCode, REQUEST_ERROR
            ) {
				// Quit the activity if Google Play services are not available.
                finish()
            }!!
            errorDialog.show()
        }
    }

    companion object {
        private const val REQUEST_ERROR = 0
    }
}
