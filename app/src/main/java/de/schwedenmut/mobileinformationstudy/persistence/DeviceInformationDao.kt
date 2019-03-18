package de.schwedenmut.mobileinformationstudy.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface DeviceInformationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDeviceInformation(deviceInformation: DeviceInformation)

    @Query("SELECT * FROM DeviceInformation")
    fun getDeviceInformation(): List<DeviceInformation>
}
