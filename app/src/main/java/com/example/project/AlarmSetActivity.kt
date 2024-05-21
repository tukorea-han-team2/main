package com.example.project

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AlarmSetActivity : AppCompatActivity() {
    private lateinit var locationService: LocationServiceExample
    private lateinit var alarmSet: AlarmSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarmset)

        // 위치 서비스 초기화
        locationService = LocationServiceExample(this)
        alarmSet = AlarmSet(this, locationService)

        // 추가된 부분: 알림 기준 레벨 버튼 초기화
        val button1: Button = findViewById(R.id.btnFetchCrimeData1)
        val button2: Button = findViewById(R.id.btnFetchCrimeData2)
        val button3: Button = findViewById(R.id.btnFetchCrimeData3)
        val button4: Button = findViewById(R.id.btnFetchCrimeData4)
        val button5: Button = findViewById(R.id.btnFetchCrimeData5)

        alarmSet.initializeButtons(button1, button2, button3, button4, button5)
    }
}