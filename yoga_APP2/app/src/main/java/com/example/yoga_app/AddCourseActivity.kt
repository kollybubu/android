package com.example.yoga_app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.sql.Date

class AddCourseActivity : AppCompatActivity() {

    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
    private lateinit var dayField: EditText
    private lateinit var dateField: EditText
    private lateinit var timeField: EditText
    private lateinit var capacityField: EditText
    private lateinit var durationField: EditText
    private lateinit var priceField: EditText
    private lateinit var typeField: EditText
    private lateinit var descriptionField: EditText
    private lateinit var lessonField: EditText
    private lateinit var teacherField: EditText
    private lateinit var genderField: Spinner
    private lateinit var saveButton: Button
    private var courseId: Int = -1 // Default for new courses

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        // Initialize views
        dayField = findViewById(R.id.dayField)
        dateField = findViewById(R.id.dateField)
        timeField = findViewById(R.id.timeField)
        capacityField = findViewById(R.id.capacityField)
        durationField = findViewById(R.id.durationField)
        priceField = findViewById(R.id.priceField)
        typeField = findViewById(R.id.typeField)
        descriptionField = findViewById(R.id.descriptionField)
        lessonField = findViewById(R.id.lessonField)
        teacherField = findViewById(R.id.teacherField)
        genderField = findViewById(R.id.genderField)
        saveButton = findViewById(R.id.saveButton)

        yogaDatabaseHelper = YogaDatabaseHelper(this)

        // Retrieve courseId passed from the previous activity
        courseId = intent.getIntExtra("courseId", -1)

        // If editing an existing course, load its details
        if (courseId != -1) {
            loadCourseDetails(courseId)
        }

        saveButton.setOnClickListener {
            saveCourse()
        }
    }

    private fun loadCourseDetails(courseId: Int) {
        val course = yogaDatabaseHelper.getCourseById(courseId)

        if (course != null) {
            dayField.setText(course.dayofweek)
            dateField.setText(course.date.toString())
            timeField.setText(course.time)
            teacherField.setText(course.teacher)
            capacityField.setText(course.capacity.toString())
            durationField.setText(course.duration)
            priceField.setText(course.price.toString())
            typeField.setText(course.typeofclass)
            lessonField.setText(course.lesson)
            descriptionField.setText(course.description)

            // Ensure the adapter is an ArrayAdapter<String>
            val genderPosition = (genderField.adapter as ArrayAdapter<String>).getPosition(course.genderOption)
            genderField.setSelection(genderPosition)
        } else {
            Toast.makeText(this, "Error loading course details", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveCourse() {
        val course = YogaCourse(
            id = if (courseId != -1) courseId else 0,
            dayofweek = dayField.text.toString(),
            date = Date(2024,7,12),//java.sql.Date.valueOf(dateField.text.toString()),
            time = timeField.text.toString(),
            capacity = capacityField.text.toString().toIntOrNull() ?: 0,
            duration = durationField.text.toString(),
            price = priceField.text.toString().toDoubleOrNull() ?: 0.0,
            typeofclass = typeField.text.toString(),
            description = descriptionField.text.toString(),
            genderOption = genderField.selectedItem?.toString() ?: "",  // Safe call for nullable
            teacher = teacherField.text.toString(),
            lesson = lessonField.text.toString()
        )

        if (courseId != -1) {
            // Convert YogaCourse object to ContentValues
            val updatedValues = yogaDatabaseHelper.toContentValues(course)
            yogaDatabaseHelper.updateCourse(courseId, updatedValues)  // Pass the content values for update
        } else {
            yogaDatabaseHelper.addCourse(course)
        }
    }


}
