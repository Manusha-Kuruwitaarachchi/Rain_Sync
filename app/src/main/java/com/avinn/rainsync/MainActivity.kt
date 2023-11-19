package com.avinn.rainsync

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var txtDate: TextView
    private lateinit var lblLocation: TextView
    private lateinit var edtCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var txtCelcius: TextView
    private lateinit var lblDescription: TextView
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private val apiKey = "bf4f8467b9b37f90499e45cac3904b60"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtDate = findViewById(R.id.txt_date)
        lblLocation = findViewById(R.id.lbllocation)
        edtCity = findViewById(R.id.edtCity)
        btnSearch = findViewById(R.id.search_btn)
        txtCelcius = findViewById(R.id.txt_celcius)
        lblDescription = findViewById(R.id.lblDescription)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        btnSearch.setOnClickListener {
            val cityName = edtCity.text.toString()
            if (cityName.isNotEmpty()) {
                getCityCountry(cityName)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestLocationUpdates()
        }
    }

    private fun requestLocationUpdates() {
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                updateLocation(location)
                locationManager.removeUpdates(this)
            }

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            locationListener
        )
    }

    private fun updateLocation(location: Location) {
        getCityName(location.latitude, location.longitude)

        // Format the date
        val sdf = SimpleDateFormat("d MMMM", Locale.getDefault())
        val formattedDate = sdf.format(Date())

        // Display the formatted date
        txtDate.text = formattedDate

        // Get weather data for the current location
        getWeatherData(lblLocation.text.toString())
    }

    private fun getCityName(latitude: Double, longitude: Double, isSearch: Boolean = false) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.isNullOrEmpty()) {
                lblLocation.text = "Location: Unknown"
                return
            }

            val cityName = addresses[0]?.locality
            val countryName = addresses[0]?.countryName

            // Update lblLocation based on whether the app is opened or a city is searched
            if (isSearch) {
                // Display city and country for searched city
                lblLocation.text = "$countryName"
            } else {
                // Display only the city when the app is opened
                if (cityName.isNullOrBlank()) {
                    lblLocation.text = "Location: Unknown"
                } else {
                    lblLocation.text = "$cityName"
                }
            }

            // Update the weather data for the entered city
            if (cityName != null) {
                getWeatherData(cityName)
            }
        } catch (e: IOException) {
            lblLocation.text = "Location: Error - ${e.message}"
            e.printStackTrace()
        }
    }



    private fun getCityCountry(city: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(city, 1)
            if (addresses.isNullOrEmpty()) {
                lblLocation.text = "Location: Unknown"
                return
            }

            val cityName = addresses[0]?.locality
            val countryName = addresses[0]?.countryName

            // Update lblLocation based on whether the app is opened or a city is searched
            if (cityName.isNullOrBlank()) {
                lblLocation.text = "Location: Unknown"
            } else {
                // Check if it's a search operation
                val isSearch = (cityName == city)

                // Display only the country for a searched city
                lblLocation.text = if (isSearch) {
                    countryName ?: "Unknown"
                } else {
                    "$countryName"
                }
            }

            // Update the weather data for the entered city
            getWeatherData(city)
        } catch (e: IOException) {
            lblLocation.text = "Location: Error"
            e.printStackTrace()
        }
    }


    private fun getWeatherData(city: String) {
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"
        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { data ->
                try {
                    val temperature = data.getJSONObject("main").getDouble("temp")
                    val weatherArray = data.getJSONArray("weather")
                    val description = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("description")
                    } else {
                        ""
                    }

                    // Update the temperature and weather description TextViews
                    txtCelcius.text = "$temperature°C"
                    lblDescription.text = description
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error parsing weather information",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("WeatherApp", "Error parsing weather information", e)
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error loading weather information",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("WeatherApp", "Volley error: $error")
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
