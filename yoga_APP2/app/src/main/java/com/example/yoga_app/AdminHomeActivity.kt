package com.example.yoga_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBox: EditText
    private lateinit var yogaClassAdapter: YogaClassAdapter
    private lateinit var manageCourseButton: Button
    private lateinit var addClassButton: Button
    private lateinit var uploadToServerButton: Button
    private lateinit var yogaDatabaseHelper: YogaDatabaseHelper

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Activity.RESULT_OK == it.resultCode) {
            searchBox.setText("")
            refreshYogaClassList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        recyclerView = findViewById(R.id.recyclerViewClasses)
        searchBox = findViewById(R.id.searchBox)
        manageCourseButton = findViewById(R.id.manageCourseButton)
        addClassButton = findViewById(R.id.addClassButton)
        uploadToServerButton = findViewById(R.id.uploadToServerButton)
        yogaDatabaseHelper = YogaDatabaseHelper(this)

        manageCourseButton.setOnClickListener {
//             Navigate to AddCourseActivity
//             Use an intent to go to the AddCourseActivity (uncomment below when AddCourseActivity is implemented)
            val intent = Intent(this, CourseListActivity::class.java)
            startActivity(intent)
        }

        addClassButton.setOnClickListener {
            // Navigate to AddClassActivity
            val intent = Intent(this, AddYogaClassActivity::class.java)
            activityResultLauncher.launch(intent)
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        uploadToServerButton.setOnClickListener {
            uploadToFirestore()
        }

        loadCourses()
    }

    private fun uploadToFirestore() {
        val db = Firebase.firestore

        val courses = yogaDatabaseHelper.getAllCourses()
        val classes = yogaDatabaseHelper.getAllYogaClasses()

        lifecycleScope.launch {
            var courseMap: HashMap<String, Any>
            courses.forEach {
                courseMap = it.toMap()

                db.collection("Courses")
                    .document("${it.id}")
                    .set(courseMap, SetOptions.merge())
                    .addOnSuccessListener { documentReference ->
//                        Log.d("DocumentReference added with ID", documentReference)
//                        Toast.makeText(applicationContext, "Successfully uploaded the courses!", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        Toast.makeText(
                            applicationContext,
                            "Failure courses updated!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .await()
            }

            prepareClassMap(classes, db)
            Toast.makeText(
                applicationContext,
                "Successfully uploaded the data!",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private suspend fun prepareClassMap(
        classes: MutableList<YogaClass>,
        db: FirebaseFirestore
    ): HashMap<String, Any> {
        var classMap = hashMapOf<String, Any>()
        classes.forEach {
            db.collection("Courses")
                .whereEqualTo("id", (it.yogaCourse.id))
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    val resultMap = hashMapOf<String, Any>()
                    resultMap["id"] = it.id
                    resultMap["date"] = it.date
                    resultMap["teacher"] = it.teacher
                    resultMap["additionalComment"] = it.additionalComment
                    resultMap["yogaCourse"] = documents.documents[0].reference

                    classMap = resultMap

                    db.collection("Classes")
                        .document("${it.id}")
                        .set(classMap, SetOptions.merge())
                        .addOnSuccessListener { documentReference ->
//                            Log.d("DocumentReference added with ID", documentReference.toString())
//                            Toast.makeText(applicationContext, "Successfully uploaded the classes!", Toast.LENGTH_LONG)
//                                .show()
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            Toast.makeText(
                                applicationContext,
                                "Failure classes updated!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .await()
        }
        return classMap
    }

    private fun loadCourses() {
        // Retrieve all classes from the database
        val classList = yogaDatabaseHelper.getAllYogaClasses()
        yogaClassAdapter = YogaClassAdapter(this, classList, yogaDatabaseHelper) { yogaClass ->
            showYogaClassDetailPopup(yogaClass)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = yogaClassAdapter
    }

    private fun showYogaClassDetailPopup(yogaClass: YogaClass) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_yoga_class, null)

        // Bind the course details to the popup view
        val dateTextView = popupView.findViewById<TextView>(R.id.popupDate)
        val dayTextView = popupView.findViewById<TextView>(R.id.popupDay)
        val teacherTextView = popupView.findViewById<TextView>(R.id.popupTeacher)

        dateTextView.text = "Date: ${yogaClass.date}"
        teacherTextView.text = "Yoga Teacher: ${yogaClass.teacher}"
        dayTextView.text = "Day: ${yogaClass.yogaCourse.dayOfWeek}"

        // Set up an alert dialog for the course details popup
        val builder = AlertDialog.Builder(this)
        builder.setView(popupView)

        // Set up the positive button to close the popup
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

        // Add Edit and Delete options to the dialog
        builder.setNeutralButton("Detail") { _, _ ->
            val intent = Intent(this, AddYogaClassActivity::class.java)
            intent.putExtra("yogaClassId", yogaClass.id)
            intent.putExtra("disabledForm", true)
            activityResultLauncher.launch(intent)
        }

        builder.setNegativeButton("Delete") { _, _ ->
            // Ask for confirmation before deleting the course
            showDeleteConfirmationDialog(yogaClass)
        }

        builder.show()
    }

    private fun showDeleteConfirmationDialog(yogaClass: YogaClass) {
        val deleteDialogBuilder = AlertDialog.Builder(this)
        deleteDialogBuilder.setMessage("Are you sure you want to delete this class?")
        deleteDialogBuilder.setPositiveButton("Yes") { _, _ ->
            // Delete the course from the database
            val result = yogaDatabaseHelper.deleteYogaClass(yogaClass.id)
            if (result > 0) {
                // Successfully deleted the course, refresh the course list
                searchBox.setText("")
                refreshYogaClassList()
            }
        }
        deleteDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        deleteDialogBuilder.show()
    }

    private fun refreshYogaClassList() {
        // Reload the courses from the database and update the RecyclerView
        val classList = yogaDatabaseHelper.getAllYogaClasses()
        yogaClassAdapter.updateYogaClass(classList)
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            refreshYogaClassList() // Clear results if query is empty
            return
        }

        // Perform search by teacher, date, or day
        val results = yogaDatabaseHelper.searchClassesByTeacher(query) +
                yogaDatabaseHelper.searchClassesByDate(query) +
                yogaDatabaseHelper.searchClassesByDay(query)

        results.forEach {
            Log.d("Result: ","${it.id}, ${it.teacher}, ${it.yogaCourse.dayOfWeek}")
        }

        yogaClassAdapter.updateYogaClass(results.distinctBy { it.id })
    }

}

