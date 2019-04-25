package uk.co.massimocarli.livedatabus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class SimpleLocationService(
  val lifecycle: Lifecycle,
  context: Context,
  override val callback: LocationCallback? = null
) : LocationStartedService(context, callback), LifecycleObserver, LocationListener by emptyLocationListener {

  companion object {
    const val LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER
  }

  val locationManager: LocationManager
  var running = false
    internal set

  init {
    locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  }

  @SuppressLint("MissingPermission")
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  override fun start() {
    if (running) {
      return
    }
    // We start to listen to the location provider
    locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0f, this)
    val enabled = locationManager.isProviderEnabled(LOCATION_PROVIDER)
    // We get a first location information
    val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LOCATION_PROVIDER)
    notifyLocation(lastKnownLocation)
    running = true
  }

  @SuppressLint("MissingPermission")
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  override fun stop() {
    if (!running) {
      return
    }
    locationManager.removeUpdates(this)
    running = false
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


class LocationPermissionDecorator(
  val decoratee: LocationStartedService
) : LocationStartedService(decoratee.context, decoratee.callback), LifecycleObserver,
  LocationListener by emptyLocationListener {

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  override fun start() {
    if (ContextCompat.checkSelfPermission(
        decoratee.context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      decoratee.start()
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  override fun stop() {
    // We need to implement the stop of the LocationService
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      decoratee.stop()
    }
  }
}

