package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.project.gs.MatchingLevelManager


class Alarmset : AppCompatActivity() {

    private var matchingLevelManager = MatchingLevelManager(4)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarmset)

        val btnFetchCrimeData1 = findViewById<Button>(R.id.btnFetchCrimeData1)
        val btnFetchCrimeData2 = findViewById<Button>(R.id.btnFetchCrimeData2)
        val btnFetchCrimeData3 = findViewById<Button>(R.id.btnFetchCrimeData3)
        val btnFetchCrimeData4 = findViewById<Button>(R.id.btnFetchCrimeData4)
        val btnFetchCrimeData5 = findViewById<Button>(R.id.btnFetchCrimeData5)

        btnFetchCrimeData1.setOnClickListener {
            // matchingLevelManager 설정 코드 추가
            matchingLevelManager.setLevel(1)

            // ActivityXmlActivity로 이동하는 Intent 생성
            val intent = Intent(this, MainActivity::class.java)

            // 액티비티 시작
            startActivity(intent)

            // 현재 액티비티 종료
            finish()
        }


        btnFetchCrimeData2.setOnClickListener {
            matchingLevelManager.setLevel(2)
        }

        btnFetchCrimeData3.setOnClickListener {
            matchingLevelManager.setLevel(3)
        }

        btnFetchCrimeData4.setOnClickListener {
            matchingLevelManager.setLevel(4)
        }

        btnFetchCrimeData5.setOnClickListener {
            matchingLevelManager.setLevel(5)
        }

    }

}
