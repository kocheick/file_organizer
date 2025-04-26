package com.shevapro.filesorter.model


data class UITaskRecord(
    val extension: String,
    val source: String,
    val destination: String,
    val isActive: Boolean = false,
    val errorMessage:String? = null,
    val id: Int = -1
) {
    fun toTaskRecord(): TaskRecord = TaskRecord(extension, source, destination, isActive, id)

    companion object {
        val EMPTY_OBJECT = UITaskRecord("", "", "", id = 0)
    }
}
