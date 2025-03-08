package androidguy.car.app

import android.car.Car
import android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.property.CarPropertyManager.CarPropertyEventCallback
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var car: Car
    private lateinit var carPropertyManager: CarPropertyManager
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var text: TextView
    private val MAX_UPDATE_RATE_HZ = 100f;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text = findViewById(R.id.idReady)
        initCarPropertyManger()
    }


    override fun onStop() {
        super.onStop()
        if (car.isConnected) {
            car.disconnect()
        }
    }

    private fun initCarPropertyManger() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE) && !::car.isInitialized) {
            car = Car.createCar(this);
            carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
            onCarServiceReady()
        }
    }

    private fun onCarServiceReady() {
        if (carPropertyManager.isPropertyAvailable(
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                VEHICLE_AREA_TYPE_GLOBAL
            )
        ) {
            carPropertyManager.registerCallback(
                callback,
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                MAX_UPDATE_RATE_HZ
            )
        } else {
            Log.e(TAG, "Vehicle speed property not available!")
        }
    }

    private val callback: CarPropertyEventCallback = object : CarPropertyEventCallback {
        override fun onChangeEvent(p: CarPropertyValue<*>?) {
            text.text = "Speed: ${p?.value} km/h"
        }

        override fun onErrorEvent(p0: Int, p1: Int) {
            text.text = "ERROR: Unable to read speed"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        carPropertyManager.unregisterCallback(callback)
    }
}