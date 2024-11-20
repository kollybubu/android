package com.example.yoga_app

import com.google.firebase.firestore.Exclude
import java.io.Serializable

class YogaCourse(
    val id: Int,
    val dayOfWeek: String,  // Make sure this matches COLUMN_DAY
    val time: String,       // Make sure this matches COLUMN_TIME
    val capacity: Int,      // Make sure this matches COLUMN_CAPACITY
    val duration: String,   // Make sure this matches COLUMN_DURATION
    val price: Double,      // Make sure this matches COLUMN_PRICE
    val typeOfClass: String, // Make sure this matches COLUMN_TYPE
    val description: String, // Make sure this matches COLUMN_DESCRIPTION
): Serializable {

    @Exclude
    fun toMap(): HashMap<String, Any> {
        val result = hashMapOf<String, Any>()
        result["id"] = id
        result["dayOfWeek"] = dayOfWeek
        result["time"] = time
        result["capacity"] = capacity
        result["duration"] = duration
        result["price"] = price
        result["typeOfClass"] = typeOfClass
        result["description"] = description

        return result
    }
}

