package com.example.fileorganizer.model

import com.example.fileorganizer.TaskRecord


data class UITaskRecord( val extension: String,
                        val source: String,
                  val destination: String,
                           val id: Int) {
    fun toTaskRecord():TaskRecord = TaskRecord(extension, source, destination, id)

    companion object {
        val EMPTY_ITEM = UITaskRecord("","","",-1)
    }
}
