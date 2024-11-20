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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CourseListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var addCourseButton: Button

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Activity.RESULT_OK == it.resultCode) {
            refreshCourseList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)

        recyclerView = findViewById(R.id.recyclerViewCourses)
        addCourseButton = findViewById(R.id.addCourseButton)

        yogaDatabaseHelper = YogaDatabaseHelper(this)

        addCourseButton.setOnClickListener {
//             Navigate to AddCourseActivity
//             Use an intent to go to the AddCourseActivity (uncomment below when AddCourseActivity is implemented)
             val intent = Intent(this, AddCourseActivity::class.java)
            activityResultLauncher.launch(intent)
        }

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
        val dayTextView = popupView.findViewById<TextView>(R.id.popupDay)
        val timeTextView = popupView.findViewById<TextView>(R.id.popupTime)
        val capacityTextView = popupView.findViewById<TextView>(R.id.popupCapacity)
        val durationTextView = popupView.findViewById<TextView>(R.id.popupDuration)
        val priceTextView = popupView.findViewById<TextView>(R.id.popupPrice)
        val typeTextView = popupView.findViewById<TextView>(R.id.popupType)
        val descriptionTextView = popupView.findViewById<TextView>(R.id.popupDescription)

        dayTextView.text = "Day: ${course.dayOfWeek}"
        timeTextView.text = "Time: ${course.time}"
        capacityTextView.text = "Capacity: ${course.capacity}"
        durationTextView.text = "Duration: ${course.duration}"
        priceTextView.text = "Price: £${course.price}"
        typeTextView.text = "Type of class: ${course.typeOfClass}"
        descriptionTextView.text = "Description: ${course.description}"

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
                refreshCourseList()
            } else {
                Toast.makeText(this, "You can't delete this course because the course is owned by the class.", Toast.LENGTH_LONG).show()
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

}

