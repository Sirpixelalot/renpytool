package com.renpytool.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renpytool.editor.model.Searcher
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.EditorSearcher

@Composable
fun SearchPanel(
    codeEditor: CodeEditor,
    searcher: Searcher
) {
    fun codeEditorSearcher() = codeEditor.searcher
    fun hasQuery() =
        searcher.query.isNotEmpty() && codeEditorSearcher().matchedPositionCount > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceContainer)
            .padding(8.dp)
    ) {
        LaunchedEffect(Unit) {
            if (searcher.query.isNotEmpty()) {
                codeEditor.searcher.search(
                    searcher.query,
                    EditorSearcher.SearchOptions(!searcher.caseSensitive, searcher.useRegex)
                )
            }
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = searcher.query,
            onValueChange = {
                searcher.query = it
                if (it.isEmpty()) {
                    codeEditor.searcher.stopSearch()
                    codeEditor.invalidate()
                } else {
                    codeEditor.searcher.search(
                        it,
                        EditorSearcher.SearchOptions(!searcher.caseSensitive, searcher.useRegex)
                    )
                }
            },
            label = { Text(text = "Find") },
        )

        Spacer(modifier = Modifier.padding(4.dp))

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = searcher.replace,
            onValueChange = { searcher.replace = it },
            label = { Text(text = "Replace") },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = searcher.useRegex,
                onCheckedChange = {
                    searcher.useRegex = it
                    if (searcher.query.isEmpty()) {
                        codeEditor.searcher.stopSearch()
                        codeEditor.invalidate()
                    } else {
                        codeEditor.searcher.search(
                            searcher.query,
                            EditorSearcher.SearchOptions(
                                !searcher.caseSensitive,
                                searcher.useRegex
                            )
                        )
                    }
                }
            )
            Text(
                text = "Regex",
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Checkbox(
                checked = searcher.caseSensitive,
                onCheckedChange = {
                    searcher.caseSensitive = it
                    if (searcher.query.isEmpty()) {
                        codeEditor.searcher.stopSearch()
                        codeEditor.invalidate()
                    } else {
                        codeEditor.searcher.search(
                            searcher.query,
                            EditorSearcher.SearchOptions(
                                !searcher.caseSensitive,
                                searcher.useRegex
                            )
                        )
                    }
                }
            )
            Text(
                text = "Case Sensitive",
                color = colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    if (hasQuery()) codeEditorSearcher().replaceCurrentMatch(searcher.replace)
                }
            ) {
                Text(text = "Replace")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    if (hasQuery()) codeEditorSearcher().replaceAll(searcher.replace)
                }
            ) {
                Text(text = "All")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (hasQuery()) codeEditorSearcher().gotoPrevious()
                }
            ) {
                Text(text = "Prev")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (hasQuery()) codeEditorSearcher().gotoNext()
                }
            ) {
                Text(text = "Next")
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
