package com.ghoast.util

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

object LocationSettingsHelper {

    fun checkGpsEnabled(context: Context, onResult: (Boolean) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onResult(true) // ✅ GPS is enabled
        }

        task.addOnFailureListener {
            onResult(false) // ❌ GPS is not enabled
        }
    }

    fun requestEnableGps(context: Context) {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                val activity = context as? Activity
                try {
                    activity?.startIntentSenderForResult(
                        exception.resolution.intentSender,
                        1001, null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("LocationHelper", "❌ Σφάλμα κατά την ενεργοποίηση GPS", e)
                }
            }
        }
    }
}
