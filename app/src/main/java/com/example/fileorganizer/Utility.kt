package com.example.fileorganizer

import androidx.compose.runtime.MutableState

object Utility {
     const val MATCH_2_CHARS_AFTER_SIGN = "%[\\dA-Za-z]{2}"
     fun swapPaths(sourcePath: MutableState<String>, destPath: MutableState<String>) {
          val temp = sourcePath.value
          sourcePath.value = destPath.value
          destPath.value = temp

     }

     fun formatUriToUIString(uri:String):String = uri.substringAfterLast(":").replace("/", " > ")


}