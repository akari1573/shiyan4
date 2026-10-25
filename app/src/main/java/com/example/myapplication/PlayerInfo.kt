package com.example.myapplication

object PlayerInfo {
    var name: String = ""
    var gender: String = ""
    var phone: String = ""
    var highScore: Int = 0

    fun resetDefaults(name: String, gender: String, phone: String) {
        this.name = name
        this.gender = gender
        this.phone = phone
        this.highScore = 0
    }
}
