package com.example.yoga_app

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var YogaDatabaseHelper: YogaDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        recyclerView = findViewById(R.id.recyclerViewCourses)
        YogaDatabaseHelper = YogaDatabaseHelper(this)

        val addCourseButton = findViewById<Button>(R.id.addCourseButton)
        addCourseButton.setOnClickListener {
//             Navigate to AddCourseActivity
//             Use an intent to go to the AddCourseActivity (uncomment below when AddCourseActivity is implemented)
             val intent = Intent(this, AddCourseActivity::class.java)
             startActivity(intent)
        }

        loadCourses()
    }

    private fun loadCourses() {
        // Retrieve all courses from the database
        val courseList = YogaDatabaseHelper.getAllCourses()
        val adapter = CourseAdapter(this, courseList, YogaDatabaseHelper) { course ->
            showCourseDetailPopup(course)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
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
            val result = YogaDatabaseHelper.deleteCourse(course.id)
            if (result > 0) {
                // Successfully deleted the course, refresh the course list
                loadCourses()
            }
        }
        deleteDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        deleteDialogBuilder.show()
    }

    fun refreshCourseList() {
        // Reload the courses from the database and update the RecyclerView
        val courses = YogaDatabaseHelper.getAllCourses()

        // Pass the lambda function to the adapter
        val adapter = CourseAdapter(this, courses, YogaDatabaseHelper) { course ->
            showCourseDetailPopup(course)
        }
        recyclerView.adapter = adapter  // Assuming your RecyclerView is named recyclerView
    }

    class AdminHomeActivity : AppCompatActivity() {

        private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
        private lateinit var searchBox: EditText
        private lateinit var searchResultsRecyclerView: RecyclerView
        private lateinit var coursesAdapter: CourseAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_admin_home)

            yogaDatabaseHelper = YogaDatabaseHelper(this)

            // Initialize views
            searchBox = findViewById(R.id.searchBox)
            searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)

            // Set up RecyclerView
            searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
            coursesAdapter = CourseAdapter(this, mutableListOf(), yogaDatabaseHelper) { course ->
                displayCourseDetails(course)
            }
            searchResultsRecyclerView.adapter = coursesAdapter

            // Add a TextWatcher to listen for changes in search input
            searchBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    performSearch(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Load initial courses
            loadCourses()
        }

        private fun loadCourses() {
            val courses = yogaDatabaseHelper.getAllCourses()
            coursesAdapter.updateCourses(courses)
        }

        private fun performSearch(query: String) {
            if (query.isBlank()) {
                coursesAdapter.updateCourses(emptyList()) // Clear results if query is empty
                return
            }

            // Perform search by teacher, date, or day
            val results = yogaDatabaseHelper.searchCoursesByTeacher(query) +
                    yogaDatabaseHelper.searchCoursesByDateOrDay(query)

            coursesAdapter.updateCourses(results)
        }

        private fun displayCourseDetails(course: YogaCourse) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Course Details")
            builder.setMessage("""
            Teacher: ${course.teacher}
            Day: ${course.dayofweek}
            Date: ${course.date}
            Time: ${course.time}
            Capacity: ${course.capacity}
            Duration: ${course.duration}
            Price: ${course.price}
            Type: ${course.typeofclass}
            Gender: ${course.genderOption}
            Lesson: ${course.lesson}
            Description: ${course.description}
        """.trimIndent())
            builder.setPositiveButton("OK", null)
            builder.show()
        }
    }


}

