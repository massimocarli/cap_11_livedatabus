package uk.co.massimocarli.livedatabus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.massimocarli.livedatabus.PermissionLifecycleObserver.Companion.LOCATION_PERMISSION_REQUEST_ID
import java.util.*

class LiveDataActivity : AppCompatActivity() {

  val locationObserver = object : Observer<Location> {
    override fun onChanged(location: Location?) {
      message.setText("Location: ${location}")
    }
  }

  lateinit var locationLiveData: PermissionLiveDataDecorator<Location>
  lateinit var permissionLifecycleObserver: PermissionLifecycleObserver

  private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
    when (item.itemId) {
      R.id.navigation_home -> {
        message.setText(R.string.title_home)
        return@OnNavigationItemSelectedListener true
      }
      R.id.navigation_dashboard -> {
        message.setText(R.string.title_dashboard)
        return@OnNavigationItemSelectedListener true
      }
      R.id.navigation_notifications -> {
        message.setText(R.string.title_notifications)
        return@OnNavigationItemSelectedListener true
      }
    }
    false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    permissionLifecycleObserver = PermissionLifecycleObserver(this, lifecycle)
    lifecycle.addObserver(permissionLifecycleObserver)
    // This is the case when we use the LocationLiveData
    //locationLiveData =
        //PermissionLiveDataDecorator(this, LocationLiveData(this), Manifest.permission.ACCESS_FINE_LOCATION)
    // This is the version when we use the BetterLocationLiveData for testing reason
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    locationLiveData =
        PermissionLiveDataDecorator(this, BetterLocationLiveData(locationManager), Manifest.permission.ACCESS_FINE_LOCATION)
    // Example of a normal locationObserver
    locationLiveData.observe(this, locationObserver)
    // Example of usage of the FilteredObserver
    // locationLiveData.observe(this, FilteredObserver(locationObserver, ::isBetterLocation))
  }


  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    if (requestCode == LOCATION_PERMISSION_REQUEST_ID) {
      // In this case we check if the user has given permission
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // We can't generate the ON_START event so we notify the PermissionLiveDataDecorator that the permission is now
        // available
        locationLiveData.onActive()
      } else {
        // We cannot use the app so we explain to the user and exit
        AlertDialog.Builder(this)
          .setTitle(R.string.location_request_dialog_title)
          .setMessage(R.string.location_request_dialog_close)
          .setPositiveButton(android.R.string.ok) { dialog, which ->
            finish()
          }
          .create()
          .show()
      }
    }
  }
}
