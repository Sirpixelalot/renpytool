package com.renpytool.editor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Searcher {
    var query by mutableStateOf("")
    var replace by mutableStateOf("")
    var caseSensitive by mutableStateOf(false)
    var useRegex by mutableStateOf(false)
}
