package uk.co.massimocarli.livedatabus

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

class BetterLocationLiveData(val locationManager: LocationManager) : StartedLiveData<Location>(),
  LocationListener by emptyLocationListener {

  companion object {
    lateinit var instance: BetterLocationLiveData
    operator fun invoke(locationManager: LocationManager): BetterLocationLiveData {
      if (!::instance.isInitialized) {
        instance = BetterLocationLiveData(locationManager)
      }
      return instance
    }
  }

  @SuppressLint("MissingPermission")
  override fun onActive() {
    // We get a first location information
    val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationService.LOCATION_PROVIDER)
    postValue(lastKnownLocation)
    // We start to listen to the location provider
    locationManager.requestLocationUpdates(LocationService.LOCATION_PROVIDER, 0L, 0f, this)
  }

  override fun onInactive() {
    locationManager.removeUpdates(this)
  }

  override fun onLocationChanged(location: Location?) {
    postValue(location)
  }
}