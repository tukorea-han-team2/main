package com.example.project

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class Alarmset : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarmset)



        val btnFetchCrimeData1 = findViewById<Button>(R.id.btnFetchCrimeData1)
        val btnFetchCrimeData2 = findViewById<Button>(R.id.btnFetchCrimeData2)
        val btnFetchCrimeData3 = findViewById<Button>(R.id.btnFetchCrimeData3)
        val btnFetchCrimeData4 = findViewById<Button>(R.id.btnFetchCrimeData4)
        val btnFetchCrimeData5 = findViewById<Button>(R.id.btnFetchCrimeData5)

        btnFetchCrimeData1.setOnClickListener {
        }

        btnFetchCrimeData2.setOnClickListener {
        }

        btnFetchCrimeData3.setOnClickListener {
        }

        btnFetchCrimeData4.setOnClickListener {
        }

        btnFetchCrimeData5.setOnClickListener {
        }

    }

}
