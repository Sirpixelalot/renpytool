package com.renpytool.editor.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renpytool.editor.EditorManager
import com.renpytool.editor.model.SymbolHolder
import io.github.rosemoe.sora.widget.CodeEditor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBarView(
    editorManager: EditorManager,
    codeEditor: CodeEditor,
    symbolList: List<SymbolHolder>
) {
    val indentChar = "    "
    var currentCursor by remember { mutableIntStateOf(1) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(color = colorScheme.surfaceContainer)
    ) {
        LazyRow(modifier = Modifier.weight(1f)) {
            item {
                SymbolBox(
                    label = "Tab",
                    onClick = {
                        if (!codeEditor.isEditable) return@SymbolBox

                        if (codeEditor.cursor.isSelected) {
                            codeEditor.indentSelection()
                        } else {
                            codeEditor.insertText(indentChar, indentChar.length)
                        }
                    },
                    onLongClick = {
                        if (!codeEditor.isEditable) return@SymbolBox

                        if (codeEditor.cursor.isSelected) {
                            codeEditor.unindentSelection()
                        }
                    }
                )
            }

            items(symbolList) { symbol ->
                SymbolBox(
                    label = symbol.label,
                    onClick = {
                        if (!codeEditor.isEditable) return@SymbolBox

                        codeEditor.let {
                            if (symbol.onSelectionStart.isNotEmpty()
                                && symbol.onSelectionEnd.isNotEmpty()
                                && it.cursor.isSelected
                            ) {
                                it.pasteText(
                                    buildString {
                                        append(symbol.onSelectionStart)
                                        append(it.text.substring(it.cursor.left, it.cursor.right))
                                        append(symbol.onSelectionEnd)
                                    }
                                )
                            } else {
                                it.insertText(
                                    symbol.onClick,
                                    if (symbol.onClickLength < 0) symbol.onClick.length else symbol.onClickLength
                                )
                            }
                        }
                    },
                    onLongClick = {
                        if (!codeEditor.isEditable) return@SymbolBox

                        if (symbol.onLongClick.isNotEmpty()) codeEditor.insertText(
                            symbol.onLongClick,
                            if (symbol.onLongClickLength < 0) symbol.onLongClick.length else symbol.onLongClickLength
                        )
                    }
                )
            }
        }

        VerticalDivider()

        Row(
            Modifier.fillMaxHeight()
        ) {
            IconButton(
                modifier = Modifier.combinedClickable(
                    onClick = {
                        val cursor = codeEditor.cursor
                        if (!cursor.isSelected) {
                            // Move cursor left by 1
                            if (cursor.leftColumn > 0) {
                                codeEditor.setSelection(cursor.leftLine, cursor.leftColumn - 1)
                            } else if (cursor.leftLine > 0) {
                                val prevLineLength = codeEditor.text.getLine(cursor.leftLine - 1).length
                                codeEditor.setSelection(cursor.leftLine - 1, prevLineLength)
                            }
                        } else {
                            // Move left cursor
                            if (currentCursor == -1 && cursor.leftColumn > 0) {
                                codeEditor.setSelectionRegion(
                                    cursor.leftLine,
                                    cursor.leftColumn - 1,
                                    cursor.rightLine,
                                    cursor.rightColumn
                                )
                            } else if (currentCursor == 1 && cursor.rightColumn > 0) {
                                codeEditor.setSelectionRegion(
                                    cursor.leftLine,
                                    cursor.leftColumn,
                                    cursor.rightLine,
                                    cursor.rightColumn - 1
                                )
                            }
                        }
                    },
                    onLongClick = {
                        val cursor = codeEditor.cursor
                        if (cursor.isSelected) {
                            currentCursor = -1
                        } else {
                            // Move to line start
                            codeEditor.setSelection(cursor.leftLine, 0)
                        }
                    }
                ),
                onClick = {}
            ) {
                Icon(
                    Icons.Rounded.ChevronLeft,
                    contentDescription = "Left",
                    tint = colorScheme.onSurface
                )
            }

            IconButton(
                modifier = Modifier.combinedClickable(
                    onClick = {
                        val cursor = codeEditor.cursor
                        if (!cursor.isSelected) {
                            // Move cursor right by 1
                            val lineLength = codeEditor.text.getLine(cursor.leftLine).length
                            if (cursor.leftColumn < lineLength) {
                                codeEditor.setSelection(cursor.leftLine, cursor.leftColumn + 1)
                            } else if (cursor.leftLine < codeEditor.text.lineCount - 1) {
                                codeEditor.setSelection(cursor.leftLine + 1, 0)
                            }
                        } else {
                            // Move right cursor
                            val leftLineLength = codeEditor.text.getLine(cursor.leftLine).length
                            val rightLineLength = codeEditor.text.getLine(cursor.rightLine).length
                            if (currentCursor == -1 && cursor.leftColumn < leftLineLength) {
                                codeEditor.setSelectionRegion(
                                    cursor.leftLine,
                                    cursor.leftColumn + 1,
                                    cursor.rightLine,
                                    cursor.rightColumn
                                )
                            } else if (currentCursor == 1 && cursor.rightColumn < rightLineLength) {
                                codeEditor.setSelectionRegion(
                                    cursor.leftLine,
                                    cursor.leftColumn,
                                    cursor.rightLine,
                                    cursor.rightColumn + 1
                                )
                            }
                        }
                    },
                    onLongClick = {
                        val cursor = codeEditor.cursor
                        if (cursor.isSelected) {
                            currentCursor = 1
                        } else {
                            // Move to line end
                            val lineLength = codeEditor.text.getLine(cursor.leftLine).length
                            codeEditor.setSelection(cursor.leftLine, lineLength)
                        }
                    }
                ),
                onClick = {}
            ) {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = "Right",
                    tint = colorScheme.onSurface
                )
            }

            IconButton(onClick = {
                editorManager.hideSearchPanel(codeEditor)
                editorManager.recentFileDialog.showRecentFileDialog = true
            }) {
                Icon(
                    Icons.Rounded.Folder,
                    contentDescription = "Files",
                    tint = colorScheme.onSurface
                )
            }
        }
    }
}
