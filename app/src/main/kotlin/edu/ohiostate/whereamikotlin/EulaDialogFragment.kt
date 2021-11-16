package edu.ohiostate.whereamikotlin

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import edu.ohiostate.whereamikotlin.Utils.fromHtml
import kotlin.system.exitProcess

/**
 * Created by adamcchampion on 2014/09/22.
 */
class EulaDialogFragment : DialogFragment() {
    private fun setEulaAccepted() {
        val activity: Activity = requireActivity()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = prefs.edit()
        editor.putBoolean(getString(R.string.eula_accepted_key), true).apply()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.about_app)
            .setMessage(fromHtml(getString(R.string.eula)))
            .setPositiveButton(R.string.accept) { _: DialogInterface?, _: Int -> setEulaAccepted() }
            .setNegativeButton(R.string.decline) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
                exitProcess(1)
            }
        return builder.create()
    }
}
