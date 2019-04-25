package uk.co.massimocarli.livedatabus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

typealias LocationCallback = (Location) -> Unit

interface StartedService {

  /**
   * Starts the service
   */
  fun start()

  /**
   * Stops the service
   */
  fun stop()
}

@ExperimentalContracts
inline fun runIfGranted(context: Context, permission: String, fn: () -> Unit) {
  contract { returns(true) implies true } // WHAT'S HERE?
  if (ContextCompat.checkSelfPermission(
      context,
      permission
    ) == PackageManager.PERMISSION_GRANTED
  ) {
    fn()
  }
}

abstract class LocationStartedService(
  val context: Context,
  open val callback: LocationCallback? = null
) : StartedService, LifecycleObserver


class LocationService(
  val lifecycle: Lifecycle,
  context: Context,
  override val callback: LocationCallback? = null
) : LocationStartedService(context, callback), LifecycleObserver, LocationListener by emptyLocationListener {

  companion object {
    const val LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER
  }

  val locationManager: LocationManager
  var running = false
    private set

  init {
    locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  override fun start() {
    if (running) {
      return
    }
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      // We get a first location information
      val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LOCATION_PROVIDER)
      notifyLocation(lastKnownLocation)
      // We start to listen to the location provider
      locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0L, 0f, this)
      running = true
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  override fun stop() {
    if (!running) {
      return
    }
    // We need to implement the stop of the LocationService
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      locationManager.removeUpdates(this)
      running = false
    }
  }

  override fun onLocationChanged(location: Location?) {
    notifyLocation(location)
  }

  private fun notifyLocation(location: Location?) {
    // We implement the logic for sending location
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
      if (location != null) {
        callback?.invoke(location)
      }
    }
  }
}

val emptyLocationListener = object : LocationListener {
  override fun onLocationChanged(location: Location?) {
    // Do nothing
  }

  override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    // Do nothing
  }

  override fun onProviderEnabled(provider: String?) {
    // Do nothing
  }

  override fun onProviderDisabled(provider: String?) {
    // Do nothing
  }
}