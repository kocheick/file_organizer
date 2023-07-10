package com.example.fileorganizer.model

import com.example.fileorganizer.TaskRecord


data class UITaskRecord( val extension: String,
                        val source: String,
                  val destination: String,
                  val isActive: Boolean = false,
                           val id: Int) {
    fun toTaskRecord():TaskRecord = TaskRecord(extension, source, destination,isActive, id)
companion object{
    val EMPTY_OBJECT = UITaskRecord("","","", id = 0)
}
}
