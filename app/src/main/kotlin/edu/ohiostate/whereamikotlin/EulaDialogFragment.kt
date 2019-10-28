package edu.ohiostate.whereamikotlin

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager

/**
 * Created by adamcchampion on 2014/09/22.
 */
class EulaDialogFragment : androidx.fragment.app.DialogFragment() {

    private fun setEulaAccepted() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity!!)
        val editor = prefs.edit()
        editor.putBoolean(getString(R.string.eula_accepted_key), true).apply()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.about_app)
                .setMessage(Utils.fromHtml(getString(R.string.eula)))
                .setPositiveButton(R.string.accept) { _, _ -> setEulaAccepted() }
                .setNegativeButton(R.string.decline) { dialog, _ ->
                    dialog.cancel()
                    activity?.finish()
                }
        return builder.create()
    }
}
