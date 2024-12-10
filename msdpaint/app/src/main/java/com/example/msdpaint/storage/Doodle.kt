package com.example.msdpaint.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName="Doodles")
data class Doodle(
    @PrimaryKey
    var doodleName: String = "",
    var timestamp: Date = Date(),
    var path: String = "",
) {

    fun isDefault(): Boolean {
        return path == ""
    }

}
