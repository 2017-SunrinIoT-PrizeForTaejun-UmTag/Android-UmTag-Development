package com.practice.bluetooth40

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mMarker: Marker
    private var mBluetoothService: BluetoothService = BluetoothService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var snippet: String = "true"

        btn_umbrella_lock.setOnClickListener {
            if(snippet.equals("true")){
                snippet = "false"
                mBluetoothService.send("lock")
                mMarker.snippet = "false"
            }else{
                snippet = "true"
                mBluetoothService.send("lock")
                mMarker.snippet = "true"
            }
        }

        btn_umbrella_location.setOnClickListener {
            val status = mBluetoothService.send("u")?.get(0).toString()
            snippet = status
            val lat = mBluetoothService.send("u")?.get(1)?.toDouble()
            val lon = mBluetoothService.send("u")?.get(2)?.toDouble()
            val umbrella = LatLng(lat!!, lon!!)
            mMarker = mMap.addMarker(MarkerOptions().position(umbrella).title("Umbrella location").snippet(status))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(umbrella))
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        mMap.setMyLocationEnabled(true);
    }
}
