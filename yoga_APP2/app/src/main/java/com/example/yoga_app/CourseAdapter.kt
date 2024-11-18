package com.example.yoga_app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(
    private val context: Context,
    private var courseList: MutableList<YogaCourse>, // Change to MutableList to allow modifications
    private val YogaDatabaseHelper: YogaDatabaseHelper,
    private val onCourseClick: (YogaCourse) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    // ViewHolder class
    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseDay: TextView = view.findViewById(R.id.courseDay)
        val courseTime: TextView = view.findViewById(R.id.courseTime)
        val courseType: TextView = view.findViewById(R.id.courseType)
        val editButton: Button = view.findViewById(R.id.editButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_item, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]

        holder.courseDay.text = "Day: ${course.dayofweek}"
        holder.courseTime.text = "Time: ${course.time}"
        holder.courseType.text = "Type: ${course.typeofclass}"

        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditCourseActivity::class.java)
            intent.putExtra("course_id", course.id)
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            val rowsDeleted = YogaDatabaseHelper.deleteCourse(course.id)
            if (rowsDeleted > 0) {
                Toast.makeText(context, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                (context as AdminHomeActivity).refreshCourseList()
            } else {
                Toast.makeText(context, "Error deleting course", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener { onCourseClick(course) }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    // Update the course list and notify the adapter
    fun updateCourses(newCourses: List<YogaCourse>) {
        courseList.clear()
        courseList.addAll(newCourses)
        notifyDataSetChanged()
    }
}
