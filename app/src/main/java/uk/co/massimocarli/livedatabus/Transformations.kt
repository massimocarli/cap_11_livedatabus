package uk.co.massimocarli.livedatabus

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations


/**
 * Simple transformation for Location formatting
 */
fun formatLocation(input: LiveData<Location?>): LiveData<String> = Transformations.map(input) { loc ->
  if (loc == null) "[]" else "[${loc.latitude} - ${loc.longitude}]"
}


/*
@MainThread
fun <T, R> map(
  source: LiveData<T>,
  mapFunction: (T) -> R
): LiveData<R> {
  val result = MediatorLiveData<R>()
  result.addSource(source) { t -> result.value = mapFunction(t) }
  return result
}

@MainThread
fun <T, R> switchMap(
  source: LiveData<T>,
  switchMapFunction: (T) -> LiveData<R>
): LiveData<R> {
  val result = MediatorLiveData<R>()
  result.addSource(source, object : Observer<T> {
    var mSource: LiveData<R>? = null

    override fun onChanged(t: T) {
      val newLiveData = switchMapFunction(t)
      if (mSource === newLiveData) {
        return
      }
      if (mSource != null) {
        result.removeSource(mSource!!)
      }
      mSource = newLiveData
      if (mSource != null) {
        result.addSource(mSource!!) { y -> result.value = y }
      }
    }
  })
  return result
}
*/

/**
 * Abstraction for a repository which allows to get T element from an input I
 */
interface Repository<I, T> {

  fun find(input: I): T
}

data class Place(val name: String, val loc: Location)

/**
 * This is the Repository implementation for the Place entity
 */
class PlaceDB : Repository<Location, LiveData<Place>> {
  override fun find(input: Location): LiveData<Place> {
    // Here we create places related to the given Location
    val result = MutableLiveData<Place>()
    // We simulate the creation of 3 places for the same location
    result.postValue(Place("Place 1", input))
    result.postValue(Place("Place 2", input))
    result.postValue(Place("Place 3", input))
    return result
  }
}

/**
 * Uses the PlaceDB in order to get the places for given location
 */
fun findPlaces(db: PlaceDB, input: LiveData<Location?>): LiveData<Place> = Transformations.switchMap(input) { loc ->
  db.find(loc!!)
}

/**
 * This allows the merge of the given LiveData
 */
fun <T> mergeLocations(vararg sources: LiveData<T>): LiveData<T> {
  val result = MediatorLiveData<T>()
  for (ld in sources) {
    result.addSource(ld) { value ->
      result.postValue(value)
    }
  }
  return result
}


/**
 * Filter implementation using MediatorLiveData
 */
fun <T> filter(src: LiveData<T?>, filter: LiveDataFilter<T>): LiveData<T> {
  val result = MediatorLiveData<T>()
  var previousValue: T? = null
  result.addSource(src) { value ->
    if (filter(value, previousValue)) {
      previousValue = value
      result.postValue(value)
    }
  }
  return result
}
