package com.renpytool.editor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class WarningDialogProperties {
    var showWarningDialog by mutableStateOf(false)
    var title by mutableStateOf("")
    var message by mutableStateOf("")
    var confirmText by mutableStateOf("OK")
    var dismissText by mutableStateOf("Cancel")
    var onConfirm: () -> Unit = {}
    var onDismiss: () -> Unit = {}
}
