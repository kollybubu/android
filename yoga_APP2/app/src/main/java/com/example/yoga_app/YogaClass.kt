package com.example.yoga_app

import com.google.firebase.firestore.Exclude
import java.io.Serializable

class YogaClass(
    val id: Int,
    val date: String,
    val teacher: String,
    val additionalComment: String,
    val yogaCourse: YogaCourse
): Serializable {
    @Exclude
    fun toMap(): HashMap<String, Any> {
        val result = hashMapOf<String, Any>()
        result["id"] = id
        result["date"] = date
        result["teacher"] = teacher
        result["additionalComment"] = additionalComment
        result["yogaCourse"] = yogaCourse

        return result
    }
}

