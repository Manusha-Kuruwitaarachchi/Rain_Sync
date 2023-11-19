package com.avinn.rainsync

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Handler().postDelayed(Runnable {
            startActivity(
                Intent(
                    this@MainActivity,
                    WeatherDashBoard::class.java
                )
            )
        }, 3000) // Delay in milliseconds (2000 milliseconds = 2 seconds)

    }
}
