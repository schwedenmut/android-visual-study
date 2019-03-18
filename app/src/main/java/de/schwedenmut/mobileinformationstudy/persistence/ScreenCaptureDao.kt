package de.schwedenmut.mobileinformationstudy.persistence

import android.arch.persistence.room.*

@Dao
interface ScreenCaptureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScreenCapture(screenCapture: ScreenCapture)

    @Update
    fun updateScreenCapture(screenCapture: ScreenCapture)

    @Delete
    fun deleteScreenCapture(screenCapture: ScreenCapture)

    @Query("SELECT * FROM ScreenCaptureData ORDER BY timestamp ASC")
    fun getAllScreenCaptureData(): List<ScreenCapture>

    @Query("SELECT COUNT(id) FROM ScreenCaptureData")
    fun getRowCount(): Int
}
