package com.example.yoga_app

import java.sql.Date

class YogaCourse(
    val id: Int,
    val dayofweek: String,  // Make sure this matches COLUMN_DAY
    val date: Date,
    val time: String,       // Make sure this matches COLUMN_TIME
    val teacher: String,
    val capacity: Int,      // Make sure this matches COLUMN_CAPACITY
    val duration: String,   // Make sure this matches COLUMN_DURATION
    val price: Double,      // Make sure this matches COLUMN_PRICE
    val typeofclass: String, // Make sure this matches COLUMN_TYPE
    val lesson: String,
    val description: String, // Make sure this matches COLUMN_DESCRIPTION
    val genderOption: String // Make sure this matches COLUMN_GENDER_OPTION
)

