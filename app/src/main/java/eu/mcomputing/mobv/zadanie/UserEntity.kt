package eu.mcomputing.mobv.zadanie

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val updated: String,
    val lat: Double,
    val lon: Double,
    val radius: Double,
    val photo: String = ""
)
