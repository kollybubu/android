package com.example.yoga_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBox: EditText
    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
    private lateinit var courseAdapter: CourseAdapter

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Activity.RESULT_OK == it.resultCode) {
            searchBox.setText("")
            refreshCourseList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        recyclerView = findViewById(R.id.recyclerViewCourses)
        searchBox = findViewById(R.id.searchBox)
        yogaDatabaseHelper = YogaDatabaseHelper(this)

        val addCourseButton = findViewById<Button>(R.id.addCourseButton)
        addCourseButton.setOnClickListener {
//             Navigate to AddCourseActivity
//             Use an intent to go to the AddCourseActivity (uncomment below when AddCourseActivity is implemented)
             val intent = Intent(this, AddCourseActivity::class.java)
            activityResultLauncher.launch(intent)
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        loadCourses()
    }

    private fun loadCourses() {
        // Retrieve all courses from the database
        val courseList = yogaDatabaseHelper.getAllCourses()
        courseAdapter = CourseAdapter(this, courseList, yogaDatabaseHelper) { course ->
            showCourseDetailPopup(course)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = courseAdapter
    }

    private fun showCourseDetailPopup(course: YogaCourse) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_course_detail, null)

        // Bind the course details to the popup view
        val teacherTextView = popupView.findViewById<TextView>(R.id.popupTeacher)
        val dateTextView = popupView.findViewById<TextView>(R.id.popupDate)
        val timeTextView = popupView.findViewById<TextView>(R.id.popupTime)
        val lessonTextView = popupView.findViewById<TextView>(R.id.popupLesson)
        val genderTextView = popupView.findViewById<TextView>(R.id.popupGender)
        val priceTextView = popupView.findViewById<TextView>(R.id.popupPrice)

        teacherTextView.text = "Yoga Teacher: ${course.teacher}"
        dateTextView.text = "Date: ${course.date}"
        timeTextView.text = "Time: ${course.time}"
        lessonTextView.text = "Lesson: ${course.lesson}"
        genderTextView.text = "Gender Option: ${course.genderOption}"
        priceTextView.text = "Price: £${course.price}"

        // Set up an alert dialog for the course details popup
        val builder = AlertDialog.Builder(this)
        builder.setView(popupView)

        // Set up the positive button to close the popup
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

        // Add Edit and Delete options to the dialog
        builder.setNeutralButton("Edit") { _, _ ->
            val intent = Intent(this, AddCourseActivity::class.java)
            intent.putExtra("courseId", course.id)
            activityResultLauncher.launch(intent)
        }

        builder.setNegativeButton("Delete") { _, _ ->
            // Ask for confirmation before deleting the course
            showDeleteConfirmationDialog(course)
        }

        builder.show()
    }

    private fun showDeleteConfirmationDialog(course: YogaCourse) {
        val deleteDialogBuilder = AlertDialog.Builder(this)
        deleteDialogBuilder.setMessage("Are you sure you want to delete this course?")
        deleteDialogBuilder.setPositiveButton("Yes") { _, _ ->
            // Delete the course from the database
            val result = yogaDatabaseHelper.deleteCourse(course.id)
            if (result > 0) {
                // Successfully deleted the course, refresh the course list
                searchBox.setText("")
                refreshCourseList()
            }
        }
        deleteDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        deleteDialogBuilder.show()
    }

    fun refreshCourseList() {
        // Reload the courses from the database and update the RecyclerView
        val courses = yogaDatabaseHelper.getAllCourses()
        courseAdapter.updateCourses(courses)
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            refreshCourseList() // Clear results if query is empty
            return
        }

        // Perform search by teacher, date, or day
        val results = yogaDatabaseHelper.searchCoursesByTeacher(query) +
                yogaDatabaseHelper.searchCoursesByDateOrDay(query)

        courseAdapter.updateCourses(results)
    }

}

