package com.example.yoga_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class YogaClassAdapter(
    private val context: Context,
    private var classList: MutableList<YogaClass>,
    private val yogaDatabaseHelper: YogaDatabaseHelper,
    private val onYogaClassClick: (YogaClass) -> Unit
) : RecyclerView.Adapter<YogaClassAdapter.YogaClassViewHolder>() {

    // ViewHolder class
    class YogaClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val classDate: TextView = view.findViewById(R.id.classDate)
        val classTeacher: TextView = view.findViewById(R.id.classTeacher)
        val classDay: TextView = view.findViewById(R.id.classDay)
        val classComment: TextView = view.findViewById(R.id.classComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YogaClassViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.yoga_class_item, parent, false)
        return YogaClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: YogaClassViewHolder, position: Int) {
        val yogaClass = classList[position]

        holder.classDate.text = "Date: ${yogaClass.date}"
        holder.classTeacher.text = "Teacher: ${yogaClass.teacher}"
        holder.classDay.text = "Day: ${yogaClass.yogaCourse.dayOfWeek}"
        if (yogaClass.additionalComment.isNullOrEmpty()) {
            holder.classComment.visibility = View.GONE
        } else {
            holder.classComment.text = "Additional Comment: ${yogaClass.additionalComment}"
            holder.classComment.visibility = View.VISIBLE
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener { onYogaClassClick(yogaClass) }
    }

    override fun getItemCount(): Int {
        return classList.size
    }

    // Update the yoga class list and notify the adapter
    fun updateYogaClass(newYogaClass: List<YogaClass>) {
        classList.clear()
        classList.addAll(newYogaClass)
        notifyDataSetChanged()
    }
}
