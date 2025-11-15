package com.renpytool.editor.model

data class SymbolHolder(
    val label: String,
    val onClick: String = label,
    val onClickLength: Int = -1,
    val onLongClick: String = "",
    val onLongClickLength: Int = -1,
    val onSelectionStart: String = "",
    val onSelectionEnd: String = ""
)
