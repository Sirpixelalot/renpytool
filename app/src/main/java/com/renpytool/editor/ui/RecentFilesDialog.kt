package com.renpytool.editor.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.renpytool.editor.EditorManager
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentFilesDialog(editorManager: EditorManager, codeEditor: CodeEditor) {
    val context = LocalContext.current

    if (editorManager.recentFileDialog.showRecentFileDialog) {
        Dialog(onDismissRequest = {
            editorManager.recentFileDialog.showRecentFileDialog = false
        }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        text = "Recent Files",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        items(editorManager.recentFileDialog.getRecentFiles(editorManager)) { fileInstance ->
                            val isActiveFile =
                                fileInstance.filePath == editorManager.getFileInstance()?.filePath

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            editorManager.switchActiveFileTo(
                                                fileInstance,
                                                codeEditor
                                            ) { content, text, isSourceChanged ->
                                                codeEditor.setText(content)
                                                if (isSourceChanged) {
                                                    editorManager.showSourceFileWarningDialog {
                                                        codeEditor.setText(Content(text))
                                                    }
                                                }
                                                editorManager.recentFileDialog.showRecentFileDialog =
                                                    false
                                            }
                                        },
                                        onLongClick = {
                                            if (!isActiveFile && fileInstance.requireSave) {
                                                editorManager.showSaveFileBeforeClose(fileInstance)
                                            } else if (!isActiveFile) {
                                                editorManager.fileInstanceList.remove(fileInstance)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "This file is currently open",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth()) {
                                    if (isActiveFile) {
                                        Spacer(
                                            Modifier
                                                .height(48.dp)
                                                .padding(vertical = 8.dp)
                                                .width(6.dp)
                                                .background(
                                                    color = colorScheme.primary,
                                                    shape = RoundedCornerShape(0, 50, 50, 0)
                                                )
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Column(
                                        modifier = Modifier
                                            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = "${if (fileInstance.requireSave) "* " else ""}${fileInstance.fileName}",
                                            fontWeight = if (isActiveFile) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = fileInstance.filePath,
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
