/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 10/19/20 10:39 AM
 * Last modified 10/19/20 10:36 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.Utils.UI.NotifyUser

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.ViewGroup
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import net.geeksempire.vicinity.android.R

interface SnackbarActionHandlerInterface {
    fun onActionButtonClicked(snackbar: Snackbar) {}
    fun onSnackbarShows(snackbar: Snackbar) {}
    fun onSnackbarDismissed(snackbar: Snackbar) {}
}

/**
 *
 **/
class SnackbarBuilder(private val context: Context) {

    fun show(rootView: ViewGroup,
             messageText: String,
             messageDuration: Int = Snackbar.LENGTH_LONG,
             actionButtonText: Int = android.R.string.ok,
             messageTextColor: Int = context.getColor(R.color.light),
             actionButtonTextColor: Int = context.getColor(R.color.pink),
             backgroundColor: Int = context.getColor(R.color.default_color_dark),
             autoDismiss: Boolean = false,
             autoDismissDuration: Long = 1000,
             snackbarActionHandlerInterface: SnackbarActionHandlerInterface) : Snackbar {

        val snackbar: Snackbar = Snackbar.make(
            rootView,
            Html.fromHtml(messageText, Html.FROM_HTML_MODE_LEGACY),
            if (autoDismiss){ Snackbar.LENGTH_INDEFINITE } else { messageDuration }
        )
        snackbar.setTextColor(messageTextColor)
        snackbar.setActionTextColor(actionButtonTextColor)
        snackbar.setBackgroundTint(backgroundColor)
        snackbar.setAction(actionButtonText) {

            snackbarActionHandlerInterface.onActionButtonClicked(snackbar)

        }
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {

            override fun onShown(transientBottomBar: Snackbar?) {
                super.onShown(transientBottomBar)

                snackbarActionHandlerInterface.onSnackbarShows(snackbar)

            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)

                snackbarActionHandlerInterface.onSnackbarDismissed(snackbar)

            }

        })
        snackbar.show()

        if (autoDismiss) {

            Handler(Looper.getMainLooper()).postDelayed({
                snackbar.dismiss()
            }, autoDismissDuration)

        }

        return snackbar
    }
}