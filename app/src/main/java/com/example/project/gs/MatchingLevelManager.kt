package com.example.project.gs

class MatchingLevelManager(private var level: Int) {

    fun getLevel(): Int {
        return level
    }

    fun setLevel(newLevel: Int) {
        if (newLevel in 1..5) {
            level = newLevel
        } else {
            throw IllegalArgumentException("Level must be between 1 and 5")
        }
    }
}