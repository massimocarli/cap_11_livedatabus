package uk.co.massimocarli.livedatabus

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

open class StartedLiveData<T> : LiveData<T>() {

  public override fun onActive() {
    super.onActive()
  }

  public override fun onInactive() {
    super.onInactive()
  }
}

class LocationLiveData(val context: Context) : StartedLiveData<Location>(), LocationListener by emptyLocationListener {

  companion object {
    lateinit var instance: LocationLiveData
    operator fun invoke(context: Context): LocationLiveData {
      if (!::instance.isInitialized) {
        instance = LocationLiveData(context)
      }
      return instance
    }
  }

  val locationManager: LocationManager

  init {
    locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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


class PermissionLiveDataDecorator<T>(
  val context: Context,
  val decoratee: StartedLiveData<T>,
  val permission: String
) : LiveData<T>(),
  LocationListener by emptyLocationListener {

  companion object {
    operator fun <T> invoke(
      context: Context,
      decoratee: StartedLiveData<T>,
      permission: String
    ): PermissionLiveDataDecorator<T> {
      return PermissionLiveDataDecorator(context, decoratee, permission)
    }
  }

  public override fun onActive() {
    if (ContextCompat.checkSelfPermission(
        context,
        permission
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      decoratee.onActive()
    }
  }

  override fun onInactive() {
    // We need to implement the stop of the LocationService
    if (ContextCompat.checkSelfPermission(
        context,
        permission
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      decoratee.onInactive()
    }
  }

  override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
    decoratee.observe(owner, observer)
  }
}

/**
 * This is the alias for the type of the function to use for the filter
 */
typealias LiveDataFilter<T> = (newValue: T?, oldValue: T?) -> Boolean

/**
 * This is a Observer which adds a filter in order to see if the new data is better than the previous. It always
 * returns the one considered better
 */
class FilteredObserver<T>(val decoratee: Observer<in T>, val filter: LiveDataFilter<in T?>) : Observer<T> {

  var previousValue: T? = null

  override fun onChanged(value: T) {
    if (filter(value, previousValue)) {
      previousValue = value
      decoratee.onChanged(value)
    } else {
      decoratee.onChanged(previousValue)
    }
  }
}

/**
 * This is a StartedLiveData for MutableLiveData. It exposes all the protected method
 */
open class StartedMutableLiveData<T> : MutableLiveData<T>() {

  public override fun onActive() {
    super.onActive()
  }

  public override fun onInactive() {
    super.onInactive()
  }
}


/**
 * Filtered version of a LiveData
 */
class FilteredLiveData<T>(val decoratee: StartedMutableLiveData<T>, val filter: LiveDataFilter<in T?>) : LiveData<T>() {

  var previousValue: T? = null

  public override fun onActive() = decoratee.onActive()

  public override fun onInactive() = decoratee.onInactive()

  override fun setValue(value: T) {
    if (filter(value, previousValue)) {
      previousValue = value
      decoratee.value = value
    } else {
      decoratee.value = previousValue
    }
  }

  override fun postValue(value: T) {
    if (filter(value, previousValue)) {
      previousValue = value
      decoratee.postValue(value)
    }
  }
}