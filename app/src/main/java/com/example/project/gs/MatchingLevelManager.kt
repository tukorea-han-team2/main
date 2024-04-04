package com.example.project.gs

class MatchingLevelManager(var matchingLevel: Int) {

    fun getLevel(): Int {
        return matchingLevel
    }

    fun setLevel(matchingLevel: Int) {
        this.matchingLevel = matchingLevel
    }
}