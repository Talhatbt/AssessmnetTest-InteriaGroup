package com.assessmenttest.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.assessmenttest.models.TravellingData


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
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            DB_NAME
                        )
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
    }
}