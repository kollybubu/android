package com.example.yoga_app

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditCourseActivity : AppCompatActivity() {

    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
    private var courseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)

        yogaDatabaseHelper = YogaDatabaseHelper(this)
        courseId = intent.getIntExtra("course_id", -1)

        // Fetch the course from the database
        val course: YogaCourse? = yogaDatabaseHelper.getCourseById(courseId)

        // UI elements
        val dayField: EditText = findViewById(R.id.dayField)
        val dateField: EditText = findViewById(R.id.dateField)
        val timeField: EditText = findViewById(R.id.timeField)
        val teacherField: EditText = findViewById(R.id.teacherField)
        val capacityField: EditText = findViewById(R.id.capacityField)
        val durationField: EditText = findViewById(R.id.durationField)
        val priceField: EditText = findViewById(R.id.priceField)
        val typeField: EditText = findViewById(R.id.typeField)
        val lessonField: EditText = findViewById(R.id.lessonField)
        val descriptionField: EditText = findViewById(R.id.descriptionField)
        val genderField: EditText = findViewById(R.id.genderField)
        val updateButton: Button = findViewById(R.id.updateButton)

        // Pre-fill fields with existing course data if available
        if (course != null) {
            dayField.setText(course.dayofweek)
            dateField.setText(course.date.toString()) // Convert date to string
            timeField.setText(course.time)
            teacherField.setText(course.teacher)
            capacityField.setText(course.capacity.toString())
            durationField.setText(course.duration)
            priceField.setText(course.price.toString())
            typeField.setText(course.typeofclass)
            lessonField.setText(course.lesson)
            descriptionField.setText(course.description)
            genderField.setText(course.genderOption)
        } else {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show()
            finish() // Close activity if the course doesn't exist
            return
        }

        // Update course logic
        updateButton.setOnClickListener {
            try {
                val updatedValues = ContentValues().apply {
                    put(YogaDatabaseHelper.COLUMN_DAY, dayField.text.toString())
                    put(YogaDatabaseHelper.COLUMN_DATE, dateField.text.toString()) // Assuming date is stored as string
                    put(YogaDatabaseHelper.COLUMN_TIME, timeField.text.toString())
                    put(YogaDatabaseHelper.COLUMN_TEACHER, teacherField.text.toString())
                    put(YogaDatabaseHelper.COLUMN_CAPACITY, capacityField.text.toString().toIntOrNull() ?: 0)
                    put(YogaDatabaseHelper.COLUMN_DURATION, durationField.text.toString())
                    put(YogaDatabaseHelper.COLUMN_PRICE, priceField.text.toString().toDoubleOrNull() ?: 0.0)
                    put(YogaDatabaseHelper.COLUMN_TYPE, typeField.text.toString())
                    put(YogaDatabaseHelper.COLUMN_LESSON, lessonField.text.toString())
                    put(YogaDatabaseHelper.COLUMN_DESCRIPTION, descriptionField.text.toString())
                    put(YogaDatabaseHelper.COLUMN_GENDER_OPTION, genderField.text.toString())
                }

                val rowsUpdated = yogaDatabaseHelper.updateCourse(courseId, updatedValues)
                if (rowsUpdated > 0) {
                    Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error updating course", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
