package com.example.project

import android.content.Context
import android.widget.Button
import android.widget.Toast

class AlarmSet(private val context: Context, private val locationService: LocationServiceExample) {

    fun initializeButtons(
        button1: Button,
        button2: Button,
        button3: Button,
        button4: Button,
        button5: Button
    ) {
        button1.setOnClickListener {
            setLevelAndStartLocationUpdates(1)
        }
        button2.setOnClickListener {
            setLevelAndStartLocationUpdates(2)
        }
        button3.setOnClickListener {
            setLevelAndStartLocationUpdates(3)
        }
        button4.setOnClickListener {
            setLevelAndStartLocationUpdates(4)
        }
        button5.setOnClickListener {
            setLevelAndStartLocationUpdates(5)
        }
    }

    private fun setLevelAndStartLocationUpdates(level: Int) {
        locationService.stopLocationUpdates()
        locationService.setSelectedLevel(level)  // Update the selected level
        locationService.startLocationUpdates()
        Toast.makeText(context, "Level $level 설정됨", Toast.LENGTH_SHORT).show()
    }
}