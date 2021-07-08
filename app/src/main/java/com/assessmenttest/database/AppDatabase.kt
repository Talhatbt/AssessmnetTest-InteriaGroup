package com.assessmenttest.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.assessmenttest.models.TravellingData
import java.io.*


@Database(entities = [TravellingData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun travelDao(): TravellingDao?

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "Assessment.db"


        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                            //.allowMainThreadQueries() // Uncomment if you don't want to use RxJava or coroutines just yet (blocks UI thread)
                            .addCallback(object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Log.e("Assessment Databas", "populating with data...")
                                }
                            }).build()
                    }
                }
            }

            return INSTANCE!!
        }


         fun copyAttachedDatabase(context: Context, databaseName: String) {
            val dbPath: File = context.getDatabasePath(databaseName)

            // If the database already exists, return
            if (dbPath.exists()) {
                return
            }

            // Make sure we have a path to the file
            dbPath.getParentFile().mkdirs()

            // Try to copy database file
            try {
                val inputStream: InputStream = context.assets.open("database/$databaseName")
                val output: OutputStream = FileOutputStream(dbPath)
                val buffer = ByteArray(8192)
                var length: Int=0
                while (inputStream.read(buffer, 0, 8192).also({ length = it }) > 0) {
                    output.write(buffer, 0, length)
                }
                output.flush()
                output.close()
                inputStream.close()
            } catch (e: IOException) {
                Log.d("TAG", "Failed to open file", e)
                e.printStackTrace()
            }
        }
    }
}