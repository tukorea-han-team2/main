package com.example.project

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class AlarmSet(private val context: Context, private val locationService: LocationServiceExample) {

    fun initializeButtons(
        button1: Button,
        button2: Button,
        button3: Button,
        button4: Button,
        button5: Button
    ) {
        button1.setOnClickListener {
            setSelectedLevelAndStartLocationUpdates(1)
        }
        button2.setOnClickListener {
            setSelectedLevelAndStartLocationUpdates(2)
        }
        button3.setOnClickListener {
            setSelectedLevelAndStartLocationUpdates(3)
        }
        button4.setOnClickListener {
            setSelectedLevelAndStartLocationUpdates(4)
        }
        button5.setOnClickListener {
            setSelectedLevelAndStartLocationUpdates(5)
        }
    }

    fun setSelectedLevelAndStartLocationUpdates(level: Int) {
        locationService.stopLocationUpdates()
        locationService.setSelectedLevel(level)  // Update the selected level

        // Save the selected level to SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("selectedLevel", level).apply()

        // Return to MainActivity with the selected level
        if (context is Activity) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("SELECTED_LEVEL", level)
            context.startActivity(intent)
            (context as Activity).finish()
        }

        locationService.startLocationUpdates()
        // 레벨이 변경된 후 팝업창 표시
        showLevelChangedPopup(level)
    }

    private fun showLevelChangedPopup(level: Int) {
        // Check if the context is still valid before showing the dialog
        if (context is Activity && !(context as Activity).isFinishing) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("알림 기준 레벨이 $level 로 변경되었습니다.")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }
}