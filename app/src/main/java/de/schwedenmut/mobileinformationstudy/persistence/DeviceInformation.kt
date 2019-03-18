package de.schwedenmut.mobileinformationstudy.persistence

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "DeviceInformation")
data class DeviceInformation(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "version") var version: Int,
    @ColumnInfo(name = "locale") var locale: String,
    @ColumnInfo(name = "model") var model: String,
    @ColumnInfo(name = "device") var device: String,
    @ColumnInfo(name = "product") var product: String,
    @ColumnInfo(name = "manufacturer") var manufacturer: String,
    @ColumnInfo(name = "sdk") var sdk: Int,
    @ColumnInfo(name = "timezone") var timezone: String,
    @ColumnInfo(name = "offset") var offset: Int,
    @ColumnInfo(name = "exportTime") var exportTime: Long) {
    override fun toString(): String {
        return "$id, $version, $locale, $model, $device, $product, $manufacturer, $sdk, $timezone, $offset, $exportTime"
    }
}
