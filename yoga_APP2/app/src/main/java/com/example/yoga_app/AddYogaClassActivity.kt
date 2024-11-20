package com.example.yoga_app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class AddYogaClassActivity : AppCompatActivity() {

    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
    private lateinit var dateField: TextView
    private lateinit var additionalCommentField: EditText
    private lateinit var teacherField: EditText
    private lateinit var courseField: TextView
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private var yogaClassId: Int = -1 // Default for new courses
    private var disabledForm: Boolean = false
    private var calendar = Calendar.getInstance()
    private var selectedCourse: YogaCourse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_yoga_class)

        // Initialize views
        dateField = findViewById(R.id.dateField)
        teacherField = findViewById(R.id.teacherField)
        additionalCommentField = findViewById(R.id.additionalCommentField)
        courseField = findViewById(R.id.courseField)
        saveButton = findViewById(R.id.saveButton)
        editButton = findViewById(R.id.editButton)

        yogaDatabaseHelper = YogaDatabaseHelper(this)

        // Retrieve courseId passed from the previous activity
        yogaClassId = intent.getIntExtra("yogaClassId", -1)
        disabledForm = intent.getBooleanExtra("disabledForm", false)

        // If editing an existing course, load its details
        if (yogaClassId != -1) {
            loadClassDetails(yogaClassId)
        }

        updateForm()

        setUpListeners()
    }

    private fun updateForm() {
        if (disabledForm) {
            dateField.isEnabled = false
            teacherField.isEnabled = false
            additionalCommentField.isEnabled = false
            courseField.isEnabled = false
            saveButton.visibility = View.GONE
            editButton.visibility = View.VISIBLE
        } else {
            dateField.isEnabled = true
            teacherField.isEnabled = true
            additionalCommentField.isEnabled = true
            courseField.isEnabled = true
            editButton.visibility = View.GONE
            saveButton.visibility = View.VISIBLE
        }
    }

    private fun setUpListeners() {
        dateField.setOnClickListener {
            showDatePickerDialog()
        }

        saveButton.setOnClickListener {
            if (!formValid()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            save()
            setResult(RESULT_OK)
            finish()
        }

        editButton.setOnClickListener {
            disabledForm = false
            updateForm()
        }

        courseField.setOnClickListener {
            val courseList = yogaDatabaseHelper.getAllCourses()
            if (courseList.isEmpty()) {
                Toast.makeText(this, "Please add course first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showYogaClassDetailPopup(courseList)
        }
    }

    private fun showYogaClassDetailPopup(courseList: MutableList<YogaCourse>) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_course_list, null)

        // Bind the course details to the popup view
        val recyclerViewCourses = popupView.findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerViewCourses.layoutManager = LinearLayoutManager(this)

        // Set up an alert dialog for the course details popup
        val builder = AlertDialog.Builder(this)
        builder.setView(popupView)
        val dialog = builder.show()

        val courseAdapter = CourseAdapter(this, courseList, yogaDatabaseHelper) { course ->
            courseField.text = buildCourseDataString(course)
            selectedCourse = course
            dialog.dismiss()
        }

        recyclerViewCourses.adapter = courseAdapter
    }

    private fun loadClassDetails(id: Int) {
        val yogaClass = yogaDatabaseHelper.getYogaClassById(id)
        selectedCourse = yogaClass?.yogaCourse

        if (yogaClass != null) {
            dateField.text = yogaClass.date
            teacherField.setText(yogaClass.teacher)
            additionalCommentField.setText(yogaClass.additionalComment)
            courseField.text =
                buildCourseDataString(yogaClass.yogaCourse)
        } else {
            Toast.makeText(this, "Error loading course details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildCourseDataString(yogaCourse: YogaCourse): String {
        return buildString {
            append("Day: ${yogaCourse.dayOfWeek} ")
            append("\n")
            append(" Time: ${yogaCourse.time} ")
            append("\n ")
            append("Capacity: ${yogaCourse.capacity} ")
            append("\n ")
            append("Duration: ${yogaCourse.duration}")
            append("\n ")
            append("Price: £${yogaCourse.price}")
            append("\n ")
            append("Type: ${yogaCourse.typeOfClass}")
            append("\n ")
            append("Description: ${yogaCourse.description}")
        }
    }

    private fun formValid(): Boolean {
        var isValid = true
        if (dateField.text.isNullOrEmpty() ||
            teacherField.text.isNullOrEmpty() ||
            selectedCourse == null
        ) {
            isValid = false
        }
        return isValid
    }

    private fun save() {
        val yogaClass = YogaClass(
            id = if (yogaClassId != -1) yogaClassId else 0,
            date = dateField.text.toString(),
            teacher = teacherField.text.toString(),
            additionalComment = additionalCommentField.text.toString(),
            yogaCourse = YogaCourse(
                id = selectedCourse!!.id,
                dayOfWeek = selectedCourse!!.dayOfWeek,
                time = selectedCourse!!.time,
                capacity = selectedCourse!!.capacity,
                duration = selectedCourse!!.duration,
                price = selectedCourse!!.price,
                typeOfClass = selectedCourse!!.typeOfClass,
                description = selectedCourse!!.description
            )
        )

        if (yogaClassId != -1) {
            // Convert yogaClass object to ContentValues
            val updatedValues = yogaDatabaseHelper.toContentValues(yogaClass)
            yogaDatabaseHelper.updateYogaClass(
                yogaClassId,
                updatedValues
            )  // Pass the content values for update
        } else {
            val result = yogaDatabaseHelper.addYogaClass(yogaClass)
            Log.d("ClassAdded:", result.toString())
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
            dateField.text = String.format(
                "%s-%s-%s",
                selectedYear.toString(),
                formatDateTime(currentMonth),
                formatDateTime(selectedDay)
            )
        }

    private fun formatDateTime(input: Int): String {
        return if (input >= 10) {
            "$input"
        } else {
            "0$input"
        }
    }

}
