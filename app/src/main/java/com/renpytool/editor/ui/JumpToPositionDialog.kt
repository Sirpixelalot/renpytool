package com.renpytool.editor.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun JumpToPositionDialog(
    codeEditor: CodeEditor,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var posInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    fun parsePosition(input: String): Pair<Int, Int> {
        val parts = input.trim().split(":", ",", " ")
        return try {
            when (parts.size) {
                1 -> Pair(parts[0].toInt(), 0)
                2 -> Pair(parts[0].toInt(), parts[1].toInt())
                else -> Pair(-1, -1)
            }
        } catch (e: Exception) {
            Pair(-1, -1)
        }
    }

    LaunchedEffect(posInput) {
        val pos = parsePosition(posInput)
        error = if (posInput.isBlank()) {
            ""
        } else if (pos.first < 0 || pos.second < 0) {
            "Invalid position"
        } else {
            ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Jump to Position",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = posInput,
                    onValueChange = {
                        posInput = it
                    },
                    label = { Text(text = "Line:Column (e.g., 10:5)") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = TextFieldDefaults.colors(
                        errorIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    isError = error.isNotEmpty(),
                    supportingText = if (error.isNotEmpty()) {
                        { Text(error) }
                    } else null
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val position = parsePosition(posInput)
                            if (position.first > -1) {
                                runCatching {
                                    codeEditor.setSelection(
                                        position.first - 1,
                                        position.second - 1
                                    )
                                }.exceptionOrNull()?.let {
                                    Toast.makeText(
                                        context,
                                        "Invalid position",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            onDismiss()
                        },
                        enabled = error.isEmpty() && posInput.isNotBlank(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Go",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
