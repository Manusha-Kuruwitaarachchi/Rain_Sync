// WeatherDashBoard.kt

package com.avinn.rainsync

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherDashBoard : AppCompatActivity() {

    private lateinit var txtDataAndTime: TextView
    private lateinit var txtCountry: TextView
    private lateinit var txtCelcius2: TextView
    private lateinit var txtDescription: TextView
    private lateinit var imgWeatherImg: ImageView
    private lateinit var txtPressureDetails: TextView
    private lateinit var txtHumidityDetails: TextView
    private lateinit var txtTempDetails: TextView
    private lateinit var txtWeatherDetails: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val apiKey = "bf4f8467b9b37f90499e45cac3904b60"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_dash_board)

        txtDataAndTime = findViewById(R.id.txt_dataAndTime)
        txtCountry = findViewById(R.id.txt_country)
        txtCelcius2 = findViewById(R.id.txt_celcius2)
        txtDescription = findViewById(R.id.txt_description)
        imgWeatherImg = findViewById(R.id.img_weatherImg)

        // Initialize the additional TextView elements
        txtPressureDetails = findViewById(R.id.txt_pressureDetails)
        txtHumidityDetails = findViewById(R.id.txt_humidityDetails)
        txtTempDetails = findViewById(R.id.txt_TempDetails)
        txtWeatherDetails = findViewById(R.id.txt_WindSpeedDetails) // Updated ID

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Get the current date and time
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMMM dd (EEE) | hh:mm a", Locale.getDefault())
        val formattedDate = sdf.format(calendar.time)

        // Display the formatted date and time
        txtDataAndTime.text = formattedDate

        // Get the current location, temperature, weather description, and weather icon for the current location
        getCurrentLocation()

        // Set OnClickListener for the search button
        val btnSearch = findViewById<Button>(R.id.btn_search)
        btnSearch.setOnClickListener {
            val cityName = findViewById<EditText>(R.id.search_bar).text.toString()
            if (cityName.isNotEmpty()) {
                // Call a method to get weather information for the searched city
                getWeatherForCity(cityName)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Use fusedLocationClient to get the current location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Get and display the current temperature, weather description, and weather icon
                        getWeatherData(location.latitude, location.longitude)
                    } else {
                        // Handle the case when location is null
                        txtCountry.text = "Location: Unknown"
                    }
                }
                .addOnFailureListener { e ->
                    // Handle errors that may occur while getting the location
                    Toast.makeText(
                        this,
                        "Error getting location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val apiUrl =
            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey"
        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { data ->
                try {
                    val cityName = data.getString("name")
                    val temperature = data.getJSONObject("main").getDouble("temp")
                    val pressure = data.getJSONObject("main").getDouble("pressure")
                    val humidity = data.getJSONObject("main").getDouble("humidity")
                    val windSpeed = data.getJSONObject("wind").getDouble("speed") // New line for wind speed
                    val weatherArray = data.getJSONArray("weather")
                    val description = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("description")
                    } else {
                        ""
                    }
                    val iconCode = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("icon")
                    } else {
                        ""
                    }

                    // Display the location name, temperature, and weather description
                    txtCountry.text = cityName
                    txtCelcius2.text = "${temperature}°C"
                    txtDescription.text = description

                    // Display the additional weather details
                    txtPressureDetails.text = "$pressure hPa"
                    txtHumidityDetails.text = "$humidity%"
                    txtTempDetails.text = "$temperature°C"
                    txtWeatherDetails.text = "${windSpeed} m/s" // Updated line for wind speed

                    // Display the weather icon
                    displayWeatherIcon(iconCode)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error parsing weather information",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error loading weather information",
                    Toast.LENGTH_SHORT
                ).show()
                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun getWeatherForCity(cityName: String) {
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"
        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { data ->
                // Handle the JSON response for the searched city
                try {
                    // Extract weather information and update UI
                    val temperature = data.getJSONObject("main").getDouble("temp")
                    val pressure = data.getJSONObject("main").getDouble("pressure")
                    val humidity = data.getJSONObject("main").getDouble("humidity")
                    val windSpeed = data.getJSONObject("wind").getDouble("speed")
                    val weatherArray = data.getJSONArray("weather")
                    val description = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("description")
                    } else {
                        ""
                    }
                    val iconCode = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("icon")
                    } else {
                        ""
                    }

                    // Display the location name, temperature, and weather description
                    txtCountry.text = cityName
                    txtCelcius2.text = "${temperature}°C"
                    txtDescription.text = description

                    // Display the additional weather details
                    txtPressureDetails.text = "$pressure hPa"
                    txtHumidityDetails.text = "$humidity%"
                    txtTempDetails.text = "$temperature°C"
                    txtWeatherDetails.text = "${windSpeed} m/s"

                    // Display the weather icon
                    displayWeatherIcon(iconCode)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error parsing weather information",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error loading weather information",
                    Toast.LENGTH_SHORT
                ).show()
                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun displayWeatherIcon(iconCode: String) {
        // Construct the URL for the weather icon
        val iconUrl = "https://openweathermap.org/img/w/$iconCode.png"

        // Use Picasso library to load and display the weather icon
        Picasso.get().load(iconUrl).into(imgWeatherImg)
    }
}
