package uk.co.massimocarli.livedatabus

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner


/**
 * Test class for the LocationService
 */
@RunWith(MockitoJUnitRunner::class)
class SimpleLocationServiceTest {


  lateinit var lifeCycle: LifecycleRegistry
  lateinit var locationService: LocationStartedService
  lateinit var locationCallback: LocationCallback
  lateinit var context: Context
  lateinit var locationManager: LocationManager

  @Before
  fun setUp() {
    // We create the Mock for the Context
    context = mock(Context::class.java)
    // We need the mock for the LocationService
    locationManager = mock(LocationManager::class.java)
    // We need to inform the context to return the Mock for the LocationService
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager)
    // We create the mock for the Lifecycle
    val lifeCycleOwner: LifecycleOwner = mock(LifecycleOwner::class.java)
    // And we create the LifecycleRegistry
    lifeCycle = LifecycleRegistry(lifeCycleOwner)
    // Initialize the locationService
    locationCallback = { }
    locationService = SimpleLocationService(lifeCycle, context, locationCallback)
    // We add the observer
    lifeCycle.addObserver(locationService)
  }

  @Test
  fun whenLifecycleStarts_locationServiceStartIsInvoked() {
    lifeCycle.markState(Lifecycle.State.STARTED)
    verify(locationManager).requestLocationUpdates(
      eq(LocationService.LOCATION_PROVIDER),
      eq(0L),
      eq(0f),
      any(LocationListener::class.java)
    )
  }

  @Test
  fun whenLifecycleStops_locationServiceStartIsStopped() {
    lifeCycle.markState(Lifecycle.State.STARTED)
    lifeCycle.markState(Lifecycle.State.CREATED)
    verify(locationManager).removeUpdates(any(LocationListener::class.java))
  }


  @Test
  fun whenLifecycleStartsTwice_locationServiceStartsOnce() {
    lifeCycle.markState(Lifecycle.State.STARTED)
    lifeCycle.markState(Lifecycle.State.STARTED)
    verify(locationManager).requestLocationUpdates(
      eq(LocationService.LOCATION_PROVIDER),
      eq(0L),
      eq(0f),
      any(LocationListener::class.java)
    )
  }
}