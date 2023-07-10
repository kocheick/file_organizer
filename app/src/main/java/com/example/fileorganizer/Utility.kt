package com.example.fileorganizer

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object Utility {
     const val MATCH_2_CHARS_AFTER_SIGN = "%[\\dA-Za-z]{2}"
     fun swapPaths(sourcePath: MutableState<String>, destPath: MutableState<String>) {
          val temp = sourcePath.value
          sourcePath.value = destPath.value
          destPath.value = temp

     }

     fun formatUriToUIString(uri:String):String = uri.substringAfterLast(":").replace("/", " > ")

     val emptyInteractionSource=  object : MutableInteractionSource {
          override val interactions: Flow<Interaction>
               get() = emptyFlow()

          override suspend fun emit(interaction: Interaction) {
          }

          override fun tryEmit(interaction: Interaction): Boolean {
               return false
          }

     }


}