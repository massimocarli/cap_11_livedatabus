package uk.co.massimocarli.livedatabus

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner


/**
 * This is the test class for the LocationLiveData object
 */
@RunWith(MockitoJUnitRunner::class)
class BetterLocationLiveDataTest {

  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  lateinit var lifeCycle: LifecycleRegistry
  lateinit var observer: Observer<*>
  lateinit var liveData: BetterLocationLiveData
  lateinit var context: Context
  lateinit var location: Location
  lateinit var lifeCycleOwner: LifecycleOwner
  lateinit var locationManager: LocationManager

  @Before
  fun setUp() {
    // We create the Mock for the Context
    context = mock(Context::class.java)
    location = mock(Location::class.java)
    locationManager = mock(LocationManager::class.java)
    // We create the mock for the Lifecycle
    lifeCycleOwner = Mockito.mock(LifecycleOwner::class.java)
    // And we create the LifecycleRegistry
    lifeCycle = LifecycleRegistry(lifeCycleOwner)
    Mockito.`when`(lifeCycleOwner.lifecycle).thenReturn(lifeCycle)
    // We create the mock for the Observer
    observer = mock(Observer::class.java)
    // The LocationLiveData
    liveData = BetterLocationLiveData(locationManager)
    val observer = mock(Observer::class.java)
    liveData.observe(lifeCycleOwner, observer as Observer<in Location>)
  }

  @Test
  fun whenLifecycleStarts_locationServiceStartIsInvoked() {
    lifeCycle.markState(Lifecycle.State.STARTED)
    verify(locationManager).requestLocationUpdates(
      Mockito.eq(LocationService.LOCATION_PROVIDER),
      Mockito.eq(0L),
      Mockito.eq(0f),
      Mockito.any(LocationListener::class.java)
    )
  }

  @Test
  fun whenLifecycleStops_locationServiceStartIsStopped() {
    lifeCycle.markState(Lifecycle.State.STARTED)
    lifeCycle.markState(Lifecycle.State.CREATED)
    verify(locationManager).removeUpdates(Mockito.any(LocationListener::class.java))
  }

  @Test
  fun whenLifecycleStartsTwice_locationServiceStartsOnce() {
    lifeCycle.markState(Lifecycle.State.STARTED)
    lifeCycle.markState(Lifecycle.State.STARTED)
    verify(locationManager).requestLocationUpdates(
      Mockito.eq(LocationService.LOCATION_PROVIDER),
      Mockito.eq(0L),
      Mockito.eq(0f),
      Mockito.any(LocationListener::class.java)
    )
  }
}