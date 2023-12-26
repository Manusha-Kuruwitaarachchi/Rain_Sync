// WeatherDashBoard.kt

package com.avinn.rainsync

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
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
import java.util.TimeZone

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

    private val apiKey = "a54d40df08aad93219fb571a3baab8db"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_dash_board)

        val button: Button = findViewById(R.id.button)

        button.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@WeatherDashBoard, ForeCastDashBoard::class.java)

            startActivity(intent)
        })
        txtDataAndTime = findViewById(R.id.txt_dataAndTime)
        txtCountry = findViewById(R.id.txt_country)
        txtCelcius2 = findViewById(R.id.txt_celcius2)
        txtDescription = findViewById(R.id.txt_description)
        imgWeatherImg = findViewById(R.id.img_weatherImg)

        txtPressureDetails = findViewById(R.id.txt_pressureDetails)
        txtHumidityDetails = findViewById(R.id.txt_humidityDetails)
        txtTempDetails = findViewById(R.id.txt_TempDetails)
        txtWeatherDetails = findViewById(R.id.txt_WindSpeedDetails)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentDateTime()

        getCurrentLocation()



        val searchBar = findViewById<EditText>(R.id.search_bar)
        searchBar.setOnTouchListener { _, event ->
            val drawableRight = 2

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchBar.right - searchBar.compoundDrawables[drawableRight].bounds.width())) {
                    // The drawableRight icon was clicked
                    onSearchButtonClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun onSearchButtonClick() {
        val cityName = findViewById<EditText>(R.id.search_bar).text.toString()
        if (cityName.isNotEmpty()) {
            getWeatherForCity(cityName)
        } else {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
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
                    val temperature = String.format("%.2f", data.getJSONObject("main").getDouble("temp") - 272.15).toDouble()
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

                    txtCountry.text = cityName
                    txtCelcius2.text = "${temperature}°C"
                    txtDescription.text = description.toUpperCase()

                    // Display the additional weather details
                    txtPressureDetails.text = "$pressure hPa"
                    txtHumidityDetails.text = "$humidity%"
                    txtTempDetails.text = "$temperature°C"
                    txtWeatherDetails.text = "${windSpeed} m/s"

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
                try {
                    val temperature = String.format("%.2f", data.getJSONObject("main").getDouble("temp") - 272.15).toDouble()
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

                    txtCountry.text = cityName
                    txtCelcius2.text = "${temperature}°C"
                    txtDescription.text = description.toUpperCase()

                    txtPressureDetails.text = "$pressure hPa"
                    txtHumidityDetails.text = "$humidity%"
                    txtTempDetails.text = "$temperature°C"
                    txtWeatherDetails.text = "${windSpeed} m/s"

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

    private fun getCurrentDateTime() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMMM dd (EEE) | hh:mm a", Locale.getDefault())
        val formattedDate = sdf.format(calendar.time)

        txtDataAndTime.text = formattedDate
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        getWeatherData(location.latitude, location.longitude)
                    } else {
                        txtCountry.text = "Location: Unknown"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error getting location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun displayWeatherIcon(iconCode: String) {
        val iconUrl = "https://openweathermap.org/img/w/$iconCode.png"

        Picasso.get().load(iconUrl).into(imgWeatherImg)
    }
}
