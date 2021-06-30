package com.assessmenttest.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.assessmenttest.models.TravellingData


@Dao
interface TravellingDao {

    @RawQuery
    fun insertDataRawFormat(query: SupportSQLiteQuery): Boolean?

    @Query("SELECT * FROM travelling")
    fun getAll(): MutableList<TravellingData>

    @Query("SELECT DISTINCT date FROM travelling")
    fun getDates(): MutableList<TravellingData>

    @Query("SELECT * FROM travelling where date=:date")
    fun getTravelListByDate(date: String): MutableList<TravellingData>
}