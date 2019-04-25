package uk.co.massimocarli.livedatabus

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class PermissionLifecycleObserver(val activity: Activity, val lifecycle: Lifecycle) : LifecycleObserver {

  companion object {
    const val LOCATION_PERMISSION_REQUEST_ID = 1
    const val REQUIRED_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun requestLocationPermission() {
    if (ContextCompat.checkSelfPermission(
        activity,
        REQUIRED_PERMISSION
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      // We check if we have to provide a reason for the Location permission request
      if (ActivityCompat.shouldShowRequestPermissionRationale(activity, REQUIRED_PERMISSION)) {
        // In this case we have to show a Dialog which explain the permission request to the user
        AlertDialog.Builder(activity)
          .setTitle(R.string.location_request_dialog_title)
          .setMessage(R.string.location_request_dialog_reason)
          .setPositiveButton(android.R.string.ok) { dialog, which ->
            ActivityCompat.requestPermissions(
              activity,
              arrayOf(REQUIRED_PERMISSION),
              LOCATION_PERMISSION_REQUEST_ID
            )
          }
          .create()
          .show()
      } else {
        ActivityCompat.requestPermissions(
          activity,
          arrayOf(REQUIRED_PERMISSION),
          LOCATION_PERMISSION_REQUEST_ID
        )
      }
    }
  }
}