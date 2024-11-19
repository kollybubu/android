package com.example.yoga_app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.sql.Date

class YogaDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "YogaApp.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_COURSES = "Courses"
        const val COLUMN_ID = "id"
        const val COLUMN_DAY = "day"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_TEACHER = "teacher"
        const val COLUMN_CAPACITY = "capacity"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_PRICE = "price"
        const val COLUMN_TYPE = "type"
        const val COLUMN_LESSON = "lesson"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_GENDER_OPTION = "gender_option"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createCoursesTable = """
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DAY TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TIME TEXT NOT NULL,
                $COLUMN_TEACHER TEXT NOT NULL,
                $COLUMN_CAPACITY INTEGER NOT NULL,
                $COLUMN_DURATION TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_LESSON TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_GENDER_OPTION TEXT NOT NULL
            );
        """.trimIndent()

        db?.execSQL(createCoursesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        onCreate(db)
    }

    // Convert YogaCourse to ContentValues
    fun toContentValues(course: YogaCourse): ContentValues {
        val values = ContentValues()
        values.put(COLUMN_DAY, course.dayofweek)
        values.put(COLUMN_DATE, course.date.toString())
        values.put(COLUMN_TIME, course.time)
        values.put(COLUMN_TEACHER, course.teacher)
        values.put(COLUMN_CAPACITY, course.capacity)
        values.put(COLUMN_DURATION, course.duration)
        values.put(COLUMN_PRICE, course.price)
        values.put(COLUMN_TYPE, course.typeofclass)
        values.put(COLUMN_LESSON, course.lesson)
        values.put(COLUMN_DESCRIPTION, course.description)
        values.put(COLUMN_GENDER_OPTION, course.genderOption)
        return values
    }

    // Add new course to the database
    fun addCourse(course: YogaCourse): Long {
        val db = writableDatabase
        val contentValues = toContentValues(course)
        return db.insert(TABLE_COURSES, null, contentValues)
    }

    // Update course details in the database
    fun updateCourse(courseId: Int, updatedValues: ContentValues): Int {
        val db = writableDatabase
        return db.update(TABLE_COURSES, updatedValues, "$COLUMN_ID = ?", arrayOf(courseId.toString()))
    }

    // Get all courses from the database
    fun getAllCourses(): MutableList<YogaCourse> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COURSES,
            arrayOf(
                COLUMN_ID,
                COLUMN_DAY,
                COLUMN_DATE,
                COLUMN_TIME,
                COLUMN_TEACHER,
                COLUMN_CAPACITY,
                COLUMN_DURATION,
                COLUMN_PRICE,
                COLUMN_TYPE,
                COLUMN_LESSON,
                COLUMN_DESCRIPTION,
                COLUMN_GENDER_OPTION
            ),
            null, null, null, null, null
        )

        val courses = mutableListOf<YogaCourse>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getColumnIndex(COLUMN_ID).takeIf { it != -1 }?.let { cursor.getInt(it) } ?: 0
                val day = cursor.getColumnIndex(COLUMN_DAY).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val date = cursor.getColumnIndex(COLUMN_DATE).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val time = cursor.getColumnIndex(COLUMN_TIME).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val teacher = cursor.getColumnIndex(COLUMN_TEACHER).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val capacity = cursor.getColumnIndex(COLUMN_CAPACITY).takeIf { it != -1 }?.let { cursor.getInt(it) } ?: 0
                val duration = cursor.getColumnIndex(COLUMN_DURATION).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val price = cursor.getColumnIndex(COLUMN_PRICE).takeIf { it != -1 }?.let { cursor.getDouble(it) } ?: 0.0
                val type = cursor.getColumnIndex(COLUMN_TYPE).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val lesson = cursor.getColumnIndex(COLUMN_LESSON).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val description = cursor.getColumnIndex(COLUMN_DESCRIPTION).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
                val genderOption = cursor.getColumnIndex(COLUMN_GENDER_OPTION).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""

                courses.add(
                    YogaCourse(
                        id = id,
                        dayofweek = day,
                        date = date,
                        time = time,
                        teacher = teacher,
                        capacity = capacity,
                        duration = duration,
                        price = price,
                        typeofclass = type,
                        lesson = lesson,
                        description = description,
                        genderOption = genderOption
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return courses
    }


    // Get course by ID
    fun getCourseById(courseId: Int): YogaCourse? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COURSES, null, "$COLUMN_ID = ?", arrayOf(courseId.toString()), null, null, null
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getColumnIndex(COLUMN_ID).takeIf { it != -1 }?.let { cursor.getInt(it) } ?: 0
            val day = cursor.getColumnIndex(COLUMN_DAY).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val date = cursor.getColumnIndex(COLUMN_DATE).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val time = cursor.getColumnIndex(COLUMN_TIME).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val teacher = cursor.getColumnIndex(COLUMN_TEACHER).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val capacity = cursor.getColumnIndex(COLUMN_CAPACITY).takeIf { it != -1 }?.let { cursor.getInt(it) } ?: 0
            val duration = cursor.getColumnIndex(COLUMN_DURATION).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val price = cursor.getColumnIndex(COLUMN_PRICE).takeIf { it != -1 }?.let { cursor.getDouble(it) } ?: 0.0
            val type = cursor.getColumnIndex(COLUMN_TYPE).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val lesson = cursor.getColumnIndex(COLUMN_LESSON).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val description = cursor.getColumnIndex(COLUMN_DESCRIPTION).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""
            val genderOption = cursor.getColumnIndex(COLUMN_GENDER_OPTION).takeIf { it != -1 }?.let { cursor.getString(it) } ?: ""

            cursor.close()
            return YogaCourse(
                id = id,
                dayofweek = day,
                date = date,
                time = time,
                teacher = teacher,
                capacity = capacity,
                duration = duration,
                price = price,
                typeofclass = type,
                lesson = lesson,
                description = description,
                genderOption = genderOption
            )
        }

        cursor.close()
        return null
    }

    // Delete course by ID
    fun deleteCourse(courseId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_COURSES, "$COLUMN_ID = ?", arrayOf(courseId.toString()))
    }
    // Search courses by partial teacher name
    fun searchCoursesByTeacher(teacherName: String): List<YogaCourse> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COURSES,
            null,
            "$COLUMN_TEACHER LIKE ?",
            arrayOf("%$teacherName%"),
            null, null, null
        )
        return extractCoursesFromCursor(cursor)
    }

    // Search courses by date or day of the week
    fun searchCoursesByDateOrDay(searchQuery: String): List<YogaCourse> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COURSES,
            null,
            "$COLUMN_DATE = ? OR $COLUMN_DAY LIKE ?",
            arrayOf(searchQuery, "%$searchQuery%"),
            null, null, null
        )
        return extractCoursesFromCursor(cursor)
    }

    // Helper to extract course list from Cursor
    private fun extractCoursesFromCursor(cursor: android.database.Cursor): List<YogaCourse> {
        val courses = mutableListOf<YogaCourse>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER))
                val capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                val lesson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LESSON))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val genderOption = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER_OPTION))

                courses.add(
                    YogaCourse(
                        id = id,
                        dayofweek = day,
                        date = date,
                        time = time,
                        teacher = teacher,
                        capacity = capacity,
                        duration = duration,
                        price = price,
                        typeofclass = type,
                        lesson = lesson,
                        description = description,
                        genderOption = genderOption
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courses
    }


}
