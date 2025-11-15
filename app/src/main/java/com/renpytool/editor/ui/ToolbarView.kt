package com.renpytool.editor.ui

import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SaveAs
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renpytool.editor.EditorManager
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun ToolbarView(
    editorManager: EditorManager,
    codeEditor: CodeEditor,
    onBackPressedDispatcher: OnBackPressedDispatcher
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = colorScheme.surfaceContainer)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.onSurface
            )
        }

        Column(
            Modifier.weight(1f)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = editorManager.activityTitle,
                fontSize = 21.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorScheme.onSurface
            )
        }

        IconButton(enabled = editorManager.canUndo, onClick = { codeEditor.undo() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = "Undo",
                tint = colorScheme.onSurface
            )
        }

        IconButton(enabled = editorManager.canRedo, onClick = { codeEditor.redo() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Redo,
                contentDescription = "Redo",
                tint = colorScheme.onSurface
            )
        }

        IconButton(
            onClick = {
                editorManager.save(
                    onSaved = {
                        Toast.makeText(context, "File saved", Toast.LENGTH_SHORT).show()
                    },
                    onFailed = {
                        Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        ) {
            Icon(
                imageVector = if (editorManager.requireSaveCurrentFile) {
                    Icons.Rounded.SaveAs
                } else {
                    Icons.Rounded.Save
                },
                contentDescription = "Save",
                tint = colorScheme.onSurface
            )
        }

        IconButton(onClick = {
            editorManager.toggleSearchPanel(!editorManager.showSearchPanel, codeEditor)
        }) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = colorScheme.onSurface
            )
        }
    }
}
