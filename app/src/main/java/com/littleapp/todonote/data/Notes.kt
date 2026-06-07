package com.littleapp.todonote.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
//import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "notes_table")
@Parcelize
data class Notes(
    val title: String,
    val content: String,
    val date: LocalDateTime = LocalDateTime.now(), //Calendar.getInstance().time.toString(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) : Parcelable {

    val createdDateFormatted: String
        get() = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
}