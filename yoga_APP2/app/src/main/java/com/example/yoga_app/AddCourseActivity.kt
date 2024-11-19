package com.example.yoga_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddCourseActivity : AppCompatActivity() {

    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
    private lateinit var dayField: Spinner
    private lateinit var dateField: TextView
    private lateinit var timeField: TextView
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
    private var calendar = Calendar.getInstance()

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

        ArrayAdapter.createFromResource(
            this,
            R.array.gender,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderField.adapter = adapter
        }
    }

    private fun setUpListeners() {
        dateField.setOnClickListener {
            showDatePickerDialog()
        }

        timeField.setOnClickListener {
            showTimePickerDialog()
        }

        saveButton.setOnClickListener {
            saveCourse()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun loadCourseDetails(courseId: Int) {
        val course = yogaDatabaseHelper.getCourseById(courseId)

        if (course != null) {
            dayField.setSelection((dayField.adapter as ArrayAdapter<String>).getPosition(course.dayofweek))
            dateField.text = course.date
            timeField.text = course.time
            teacherField.setText(course.teacher)
            capacityField.setText(course.capacity.toString())
            durationField.setText(course.duration)
            priceField.setText(course.price.toString())
            typeField.setText(course.typeofclass)
            lessonField.setText(course.lesson)
            descriptionField.setText(course.description)

            // Ensure the adapter is an ArrayAdapter<String>
            val genderPosition =
                (genderField.adapter as ArrayAdapter<String>).getPosition(course.genderOption)
            genderField.setSelection(genderPosition)
        } else {
            Toast.makeText(this, "Error loading course details", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveCourse() {
        val course = YogaCourse(
            id = if (courseId != -1) courseId else 0,
            dayofweek = dayField.selectedItem.toString(),
            date = dateField.text.toString(),//java.sql.Date.valueOf(dateField.text.toString()),
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
            yogaDatabaseHelper.updateCourse(
                courseId,
                updatedValues
            )  // Pass the content values for update
        } else {
            yogaDatabaseHelper.addCourse(course)
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            datePickerListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.setCancelable(false)
//        datePicker.datePicker.maxDate = calendar.timeInMillis
        datePicker.show()
    }

    private val datePickerListener =
        DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
            val currentMonth = selectedMonth + 1
            dateField.text = String.format("%s-%s-%s", selectedYear.toString(), formatDateTime(currentMonth), formatDateTime(selectedDay))
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
