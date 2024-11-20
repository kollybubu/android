package com.example.yoga_app

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddCourseActivity : AppCompatActivity() {

    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
    private lateinit var dayField: Spinner
    private lateinit var timeField: TextView
    private lateinit var capacityField: EditText
    private lateinit var durationField: EditText
    private lateinit var priceField: EditText
    private lateinit var typeField: EditText
    private lateinit var descriptionField: EditText
//    private lateinit var genderField: Spinner
    private lateinit var saveButton: Button
    private var courseId: Int = -1 // Default for new courses
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        // Initialize views
        dayField = findViewById(R.id.dayField)
        timeField = findViewById(R.id.timeField)
        capacityField = findViewById(R.id.capacityField)
        durationField = findViewById(R.id.durationField)
        priceField = findViewById(R.id.priceField)
        typeField = findViewById(R.id.typeField)
        descriptionField = findViewById(R.id.descriptionField)
//        genderField = findViewById(R.id.genderField)
        saveButton = findViewById(R.id.saveButton)

        yogaDatabaseHelper = YogaDatabaseHelper(this)

        setUpAdapter()

        // Retrieve courseId passed from the previous activity
        courseId = intent.getIntExtra("courseId", -1)

        // If editing an existing course, load its details
        if (courseId != -1) {
            loadCourseDetails(courseId)
        }

        setUpListeners()
    }

    private fun setUpAdapter() {
        ArrayAdapter.createFromResource(
            this,
            R.array.day_of_week,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dayField.adapter = adapter
        }

        /*ArrayAdapter.createFromResource(
            this,
            R.array.gender,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderField.adapter = adapter
        }*/
    }

    private fun setUpListeners() {
        timeField.setOnClickListener {
            showTimePickerDialog()
        }

        saveButton.setOnClickListener {
            if (!formValid()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveCourse()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun loadCourseDetails(courseId: Int) {
        val course = yogaDatabaseHelper.getCourseById(courseId)

        if (course != null) {
            dayField.setSelection((dayField.adapter as ArrayAdapter<String>).getPosition(course.dayOfWeek))
            timeField.text = course.time
            capacityField.setText(course.capacity.toString())
            durationField.setText(course.duration)
            priceField.setText(course.price.toString())
            typeField.setText(course.typeOfClass)
            descriptionField.setText(course.description)

            // Ensure the adapter is an ArrayAdapter<String>
            /*val genderPosition =
                (genderField.adapter as ArrayAdapter<String>).getPosition(course.genderOption)
            genderField.setSelection(genderPosition)*/
        } else {
            Toast.makeText(this, "Error loading course details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formValid(): Boolean {
        var isValid = true
        if (timeField.text.isNullOrEmpty() ||
            capacityField.text.isNullOrEmpty() ||
            durationField.text.isNullOrEmpty() ||
            priceField.text.isNullOrEmpty() ||
            typeField.text.isNullOrEmpty()
        ) {
            isValid = false
        }
        return isValid
    }

    private fun saveCourse() {
        val course = YogaCourse(
            id = if (courseId != -1) courseId else 0,
            dayOfWeek = dayField.selectedItem.toString(),
            time = timeField.text.toString(),
            capacity = capacityField.text.toString().toIntOrNull() ?: 0,
            duration = durationField.text.toString(),
            price = priceField.text.toString().toDoubleOrNull() ?: 0.0,
            typeOfClass = typeField.text.toString(),
            description = descriptionField.text.toString()
//            genderOption = genderField.selectedItem?.toString() ?: "",
        )

        if (courseId != -1) {
            // Convert YogaCourse object to ContentValues
            val updatedValues = yogaDatabaseHelper.toContentValues(course)
            yogaDatabaseHelper.updateCourse(
                courseId,
                updatedValues
            )  // Pass the content values for update
        } else {
            val result = yogaDatabaseHelper.addCourse(course)
            Log.d("CourseAdded:", result.toString())
        }
    }

    private fun showTimePickerDialog() {
        val timePicker = TimePickerDialog(
            this,
            { view, hourOfDay, minute ->
                timeField.text = String.format("%s:%s", formatDateTime(hourOfDay), formatDateTime(minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun formatDateTime(input: Int): String {
        return if (input >= 10) {
            "$input"
        } else {
            "0$input"
        }
    }

}
