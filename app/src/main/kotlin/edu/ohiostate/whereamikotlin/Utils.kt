@file:Suppress("DEPRECATION")

package edu.ohiostate.whereamikotlin

import android.os.Build
import android.text.Html
import android.text.Spanned

object Utils {

    /*
     * Code from Stack Overflow (J. Burrows):
     * https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
     */
    fun fromHtml(htmlStr: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlStr)
        }
    }
}
