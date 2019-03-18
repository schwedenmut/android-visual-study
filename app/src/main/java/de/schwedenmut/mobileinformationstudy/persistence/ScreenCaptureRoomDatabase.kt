package de.schwedenmut.mobileinformationstudy.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(ScreenCapture::class, DeviceInformation::class), version = 2)
abstract class ScreenCaptureRoomDatabase : RoomDatabase(){
    abstract fun screenCaptureDao(): ScreenCaptureDao
    abstract fun deviceInformationDao(): DeviceInformationDao

    companion object {
        var INSTANCE: ScreenCaptureRoomDatabase? = null

        fun getDatabase(context: Context): ScreenCaptureRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(ScreenCaptureRoomDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            ScreenCaptureRoomDatabase::class.java,
                            "ScreenCaptureDatabase.db")
                            .build()
                }
            }
            return INSTANCE
        }
        fun destroyDatabase() {
            INSTANCE = null
        }
    }

}
