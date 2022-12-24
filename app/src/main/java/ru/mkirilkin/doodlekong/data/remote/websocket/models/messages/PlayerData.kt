package com.plcourse.mkirilkin.data

data class PlayerData(
    val userName: String,
    var isDrawig: Boolean = false,
    var score: Int = 0,
    var rank: Int = 0
)
