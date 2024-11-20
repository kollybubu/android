package com.example.yoga_app

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class YogaDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "YogaApp.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_COURSES = "Courses"
        const val COLUMN_ID = "id"
        const val COLUMN_DAY = "day"
        const val COLUMN_TIME = "time"
        const val COLUMN_CAPACITY = "capacity"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_PRICE = "price"
        const val COLUMN_TYPE = "type"

        //        const val COLUMN_LESSON = "lesson"
        const val COLUMN_DESCRIPTION = "description"

        //        const val COLUMN_GENDER_OPTION = "gender_option"
        const val TABLE_CLASSES = "Classes"
        const val COLUMN_CLASS_ID = "id"
        const val COLUMN_CLASS_DATE = "date"
        const val COLUMN_CLASS_TEACHER = "teacher"
        const val COLUMN_CLASS_COMMENT = "additional_comment"
        const val COLUMN_COURSE_ID = "course_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createCoursesTable = """
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DAY TEXT NOT NULL,
                $COLUMN_TIME TEXT NOT NULL,
                $COLUMN_CAPACITY INTEGER NOT NULL,
                $COLUMN_DURATION TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT
            );
        """.trimIndent()

        val createClassesTable = """
            CREATE TABLE $TABLE_CLASSES (
                $COLUMN_CLASS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CLASS_DATE TEXT NOT NULL,
                $COLUMN_CLASS_TEACHER TEXT NOT NULL,
                $COLUMN_CLASS_COMMENT TEXT,
                $COLUMN_COURSE_ID INTEGER NOT NULL
            );
        """.trimIndent()

        db?.execSQL(createCoursesTable)
        db?.execSQL(createClassesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSES")
        onCreate(db)
    }

    // Convert YogaCourse to ContentValues
    fun toContentValues(course: YogaCourse): ContentValues {
        val values = ContentValues()
        values.put(COLUMN_DAY, course.dayOfWeek)
        values.put(COLUMN_TIME, course.time)
        values.put(COLUMN_CAPACITY, course.capacity)
        values.put(COLUMN_DURATION, course.duration)
        values.put(COLUMN_PRICE, course.price)
        values.put(COLUMN_TYPE, course.typeOfClass)
        values.put(COLUMN_DESCRIPTION, course.description)
        return values
    }

    // Convert Class to ContentValues
    fun toContentValues(yogaClass: YogaClass): ContentValues {
        val values = ContentValues()
        values.put(COLUMN_CLASS_DATE, yogaClass.date)
        values.put(COLUMN_CLASS_TEACHER, yogaClass.teacher)
        values.put(COLUMN_CLASS_COMMENT, yogaClass.additionalComment)
        values.put(COLUMN_COURSE_ID, yogaClass.yogaCourse.id)
        return values
    }

    // Add new course to the database
    fun addCourse(course: YogaCourse): Long {
        val db = writableDatabase
        val contentValues = toContentValues(course)
        return db.insert(TABLE_COURSES, null, contentValues)
    }

    // Add new class to the database
    fun addYogaClass(yogaClass: YogaClass): Long {
        val db = writableDatabase
        val contentValues = toContentValues(yogaClass)
        return db.insert(TABLE_CLASSES, null, contentValues)
    }

    // Update course details in the database
    fun updateCourse(courseId: Int, updatedValues: ContentValues): Int {
        val db = writableDatabase
        return db.update(
            TABLE_COURSES,
            updatedValues,
            "$COLUMN_ID = ?",
            arrayOf(courseId.toString())
        )
    }

    // Update class details in the database
    fun updateYogaClass(classId: Int, updatedValues: ContentValues): Int {
        val db = writableDatabase
        return db.update(
            TABLE_CLASSES,
            updatedValues,
            "$COLUMN_CLASS_ID = ?",
            arrayOf(classId.toString())
        )
    }

    // Get all courses from the database
    fun getAllCourses(): MutableList<YogaCourse> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COURSES,
            arrayOf(
                COLUMN_ID,
                COLUMN_DAY,
                COLUMN_TIME,
                COLUMN_CAPACITY,
                COLUMN_DURATION,
                COLUMN_PRICE,
                COLUMN_TYPE,
                COLUMN_DESCRIPTION
            ),
            null, null, null, null, null
        )

        val courses = mutableListOf<YogaCourse>()

        if (cursor.moveToFirst()) {
            do {
                val id =
                    cursor.getColumnIndex(COLUMN_ID).takeIf { it != -1 }?.let { cursor.getInt(it) }
                        ?: 0
                val day = cursor.getColumnIndex(COLUMN_DAY).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val time = cursor.getColumnIndex(COLUMN_TIME).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val capacity = cursor.getColumnIndex(COLUMN_CAPACITY).takeIf { it != -1 }
                    ?.let { cursor.getInt(it) } ?: 0
                val duration = cursor.getColumnIndex(COLUMN_DURATION).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val price = cursor.getColumnIndex(COLUMN_PRICE).takeIf { it != -1 }
                    ?.let { cursor.getDouble(it) } ?: 0.0
                val type = cursor.getColumnIndex(COLUMN_TYPE).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val description = cursor.getColumnIndex(COLUMN_DESCRIPTION).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""

                courses.add(
                    YogaCourse(
                        id = id,
                        dayOfWeek = day,
                        time = time,
                        capacity = capacity,
                        duration = duration,
                        price = price,
                        typeOfClass = type,
                        description = description
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return courses
    }

    // Get all classes from the database
    fun getAllYogaClasses(): MutableList<YogaClass> {
        val db = readableDatabase
        val cursor = db.query(
            "$TABLE_CLASSES , $TABLE_COURSES",
            null,
            "$TABLE_CLASSES.$COLUMN_COURSE_ID = $TABLE_COURSES.$COLUMN_ID",
            null, null, null, null
        )

        val yogaClasses = mutableListOf<YogaClass>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getColumnIndex(COLUMN_CLASS_ID).takeIf { it != -1 }
                    ?.let { cursor.getInt(it) } ?: 0
                val date = cursor.getColumnIndex(COLUMN_CLASS_DATE).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val teacher = cursor.getColumnIndex(COLUMN_CLASS_TEACHER).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val additionalComment =
                    cursor.getColumnIndex(COLUMN_CLASS_COMMENT).takeIf { it != -1 }
                        ?.let { cursor.getString(it) } ?: ""
                val courseId = cursor.getColumnIndex(COLUMN_COURSE_ID).takeIf { it != -1 }
                    ?.let { cursor.getInt(it) } ?: 0
                val day = cursor.getColumnIndex(COLUMN_DAY).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val time = cursor.getColumnIndex(COLUMN_TIME).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val capacity = cursor.getColumnIndex(COLUMN_CAPACITY).takeIf { it != -1 }
                    ?.let { cursor.getInt(it) } ?: 0
                val duration = cursor.getColumnIndex(COLUMN_DURATION).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val price = cursor.getColumnIndex(COLUMN_PRICE).takeIf { it != -1 }
                    ?.let { cursor.getDouble(it) } ?: 0.0
                val type = cursor.getColumnIndex(COLUMN_TYPE).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""
                val description = cursor.getColumnIndex(COLUMN_DESCRIPTION).takeIf { it != -1 }
                    ?.let { cursor.getString(it) } ?: ""

                yogaClasses.add(
                    YogaClass(
                        id = id,
                        date = date,
                        teacher = teacher,
                        additionalComment = additionalComment,
                        yogaCourse = YogaCourse(
                            id = courseId,
                            dayOfWeek = day,
                            time = time,
                            capacity = capacity,
                            duration = duration,
                            price = price,
                            typeOfClass = type,
                            description = description
                        ),
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return yogaClasses
    }


    // Get course by ID
    fun getCourseById(courseId: Int): YogaCourse? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COURSES, null, "$COLUMN_ID = ?", arrayOf(courseId.toString()), null, null, null
        )

        if (cursor.moveToFirst()) {
            val id =
                cursor.getColumnIndex(COLUMN_ID).takeIf { it != -1 }?.let { cursor.getInt(it) } ?: 0
            val day =
                cursor.getColumnIndex(COLUMN_DAY).takeIf { it != -1 }?.let { cursor.getString(it) }
                    ?: ""
            val time =
                cursor.getColumnIndex(COLUMN_TIME).takeIf { it != -1 }?.let { cursor.getString(it) }
                    ?: ""
            val capacity = cursor.getColumnIndex(COLUMN_CAPACITY).takeIf { it != -1 }
                ?.let { cursor.getInt(it) } ?: 0
            val duration = cursor.getColumnIndex(COLUMN_DURATION).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val price = cursor.getColumnIndex(COLUMN_PRICE).takeIf { it != -1 }
                ?.let { cursor.getDouble(it) } ?: 0.0
            val type =
                cursor.getColumnIndex(COLUMN_TYPE).takeIf { it != -1 }?.let { cursor.getString(it) }
                    ?: ""
            val description = cursor.getColumnIndex(COLUMN_DESCRIPTION).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""

            cursor.close()
            return YogaCourse(
                id = id,
                dayOfWeek = day,
                time = time,
                capacity = capacity,
                duration = duration,
                price = price,
                typeOfClass = type,
                description = description
            )
        }

        cursor.close()
        return null
    }

    // Get yoga class by ID
    fun getYogaClassById(classId: Int): YogaClass? {
        val db = readableDatabase
        val cursor = db.query(
            "$TABLE_CLASSES , $TABLE_COURSES",
            null,
            "$TABLE_COURSES.$COLUMN_ID = $TABLE_CLASSES.$COLUMN_COURSE_ID AND $TABLE_CLASSES.$COLUMN_CLASS_ID = ?",
            arrayOf(classId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getColumnIndex(COLUMN_CLASS_ID).takeIf { it != -1 }
                ?.let { cursor.getInt(it) } ?: 0
            val date = cursor.getColumnIndex(COLUMN_CLASS_DATE).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val teacher = cursor.getColumnIndex(COLUMN_CLASS_TEACHER).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val additionalComment = cursor.getColumnIndex(COLUMN_CLASS_COMMENT).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val courseId = cursor.getColumnIndex(COLUMN_COURSE_ID).takeIf { it != -1 }
                ?.let { cursor.getInt(it) } ?: 0
            val day = cursor.getColumnIndex(COLUMN_DAY).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val time = cursor.getColumnIndex(COLUMN_TIME).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val capacity = cursor.getColumnIndex(COLUMN_CAPACITY).takeIf { it != -1 }
                ?.let { cursor.getInt(it) } ?: 0
            val duration = cursor.getColumnIndex(COLUMN_DURATION).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val price = cursor.getColumnIndex(COLUMN_PRICE).takeIf { it != -1 }
                ?.let { cursor.getDouble(it) } ?: 0.0
            val type = cursor.getColumnIndex(COLUMN_TYPE).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""
            val description = cursor.getColumnIndex(COLUMN_DESCRIPTION).takeIf { it != -1 }
                ?.let { cursor.getString(it) } ?: ""

            cursor.close()
            return YogaClass(
                id = id,
                date = date,
                teacher = teacher,
                additionalComment = additionalComment,
                yogaCourse = YogaCourse(
                    id = courseId,
                    dayOfWeek = day,
                    time = time,
                    capacity = capacity,
                    duration = duration,
                    price = price,
                    typeOfClass = type,
                    description = description
                )
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

    // Delete yoga class by ID
    fun deleteYogaClass(classId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_COURSES, "$COLUMN_CLASS_ID = ?", arrayOf(classId.toString()))
    }

    // Search classes by partial teacher name
    fun searchClassesByTeacher(teacherName: String): List<YogaClass> {
        val db = readableDatabase
        val cursor = db.query(
            "$TABLE_CLASSES , $TABLE_COURSES",
            null,
            "$TABLE_COURSES.$COLUMN_ID = $TABLE_CLASSES.$COLUMN_COURSE_ID AND $TABLE_CLASSES.$COLUMN_CLASS_TEACHER LIKE ?",
            arrayOf("%$teacherName%"),
            null,
            null,
            null
        )
        return extractClassesFromCursor(cursor)
    }

    // Search courses by date or day of the week
    fun searchClassesByDate(searchQuery: String): List<YogaClass> {
        val db = readableDatabase
        val cursor = db.query(
            "$TABLE_CLASSES , $TABLE_COURSES",
            null,
            "$TABLE_COURSES.$COLUMN_ID = $TABLE_CLASSES.$COLUMN_COURSE_ID AND $TABLE_CLASSES.$COLUMN_CLASS_DATE = ?",
            arrayOf(searchQuery),
            null,
            null,
            null
        )
        return extractClassesFromCursor(cursor)
    }

    // Search classes by day of the week
    fun searchClassesByDay(searchQuery: String): List<YogaClass> {
        val db = readableDatabase
        val cursor = db.query(
            "$TABLE_CLASSES , $TABLE_COURSES",
            null,
            "$TABLE_CLASSES.$COLUMN_COURSE_ID = $TABLE_COURSES.$COLUMN_ID AND $TABLE_COURSES.$COLUMN_DAY LIKE ?",
            arrayOf("%$searchQuery%"),
            null, null, null
        )
        return extractClassesFromCursor(cursor)
    }

    // Helper to extract course list from Cursor
    private fun extractCoursesFromCursor(cursor: android.database.Cursor): List<YogaCourse> {
        val courses = mutableListOf<YogaCourse>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))

                courses.add(
                    YogaCourse(
                        id = id,
                        dayOfWeek = day,
                        time = time,
                        capacity = capacity,
                        duration = duration,
                        price = price,
                        typeOfClass = type,
                        description = description,
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courses
    }

    // Helper to extract class list from Cursor
    private fun extractClassesFromCursor(cursor: android.database.Cursor): List<YogaClass> {
        val yogaClasses = mutableListOf<YogaClass>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_DATE))
                val teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_TEACHER))
                val additionalComment =
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COMMENT))
                val courseId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID))
                val day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))

                yogaClasses.add(
                    YogaClass(
                        id = id,
                        date = date,
                        teacher = teacher,
                        additionalComment = additionalComment,
                        yogaCourse = YogaCourse(
                            id = courseId,
                            dayOfWeek = day,
                            time = time,
                            capacity = capacity,
                            duration = duration,
                            price = price,
                            typeOfClass = type,
                            description = description,
                        )
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return yogaClasses
    }

}
