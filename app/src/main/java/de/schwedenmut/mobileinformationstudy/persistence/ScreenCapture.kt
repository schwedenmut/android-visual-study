package de.schwedenmut.mobileinformationstudy.persistence

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "ScreenCaptureData")
data class ScreenCapture(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "filesizePNG") var filesizePNG: Long,
    @ColumnInfo(name = "filesizeJPEG") var filesizeJPEG: Long,
    @ColumnInfo(name = "recentApplication") var recentApplication: String,
    @ColumnInfo(name = "width") var width: Int,
    @ColumnInfo(name = "height") var height: Int,
    @ColumnInfo(name = "density") var density: Int,
    @ColumnInfo(name = "elapsedPNG") var elapsedPNG: Long,
    @ColumnInfo(name = "elapsedJPEG") var elapsedJPEG: Long) {
    override fun toString(): String {
        return "$id,$timestamp,$filesizePNG,$filesizeJPEG,$recentApplication,$width,$height,$density,$elapsedPNG,$elapsedJPEG"
    }
}

