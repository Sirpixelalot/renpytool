package com.renpytool.editor

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.renpytool.RpyEditorActivityNew
import com.renpytool.editor.model.Searcher
import com.renpytool.editor.model.SymbolHolder
import com.renpytool.editor.model.WarningDialogProperties
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.subscribeAlways
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File

class EditorManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)

    var showSearchPanel by mutableStateOf(false)
    val warningDialogProperties = WarningDialogProperties()
    val recentFileDialog = RecentFileDialog()

    var activeFilePath: String = ""

    var activityTitle by mutableStateOf("")
    var activitySubtitle by mutableStateOf("")
    var showJumpToPositionDialog by mutableStateOf(false)
    var canUndo by mutableStateOf(false)
    var canRedo by mutableStateOf(false)
    private var isReading by mutableStateOf(false)
    private var isSaving by mutableStateOf(false)
    var requireSaveCurrentFile by mutableStateOf(false)
    val fileInstanceList = mutableStateListOf<FileInstance>()

    private var defaultSymbolHolders = arrayListOf(
        SymbolHolder("_"),
        SymbolHolder("="),
        SymbolHolder("{"),
        SymbolHolder("}"),
        SymbolHolder("["),
        SymbolHolder("]"),
        SymbolHolder("("),
        SymbolHolder(")"),
        SymbolHolder(":"),
        SymbolHolder("\""),
        SymbolHolder("'"),
        SymbolHolder("|"),
        SymbolHolder("+"),
        SymbolHolder("-"),
        SymbolHolder("*"),
        SymbolHolder("/"),
        SymbolHolder("<"),
        SymbolHolder(">"),
        SymbolHolder("$")
    )

    private var customSymbolHolders = arrayListOf<SymbolHolder>()
    private var textMateInitialized = false

    fun getSymbols(): ArrayList<SymbolHolder> {
        return customSymbolHolders.ifEmpty { defaultSymbolHolders }
    }

    fun hideSearchPanel(codeEditor: CodeEditor) {
        if (showSearchPanel) toggleSearchPanel(false, codeEditor)
    }

    fun toggleSearchPanel(value: Boolean = !showSearchPanel, codeEditor: CodeEditor) {
        showSearchPanel = value
        if (!value) codeEditor.searcher.stopSearch()
    }

    fun checkActiveFileValidity(
        onSourceReload: (newText: String) -> Unit,
        onFileNotFound: () -> Unit
    ) {
        getFileInstance()?.let { instance ->
            val file = File(instance.filePath)
            if (!file.exists()) {
                onFileNotFound()
                return
            }

            val currentModified = file.lastModified()
            if (currentModified != instance.lastModified) {
                scope.launch {
                    val newText = file.readText()
                    withContext(Dispatchers.Main) {
                        showSourceFileWarningDialog { onSourceReload(newText) }
                    }
                }
            }
        }
    }

    fun showSourceFileWarningDialog(onConfirm: () -> Unit) {
        warningDialogProperties.apply {
            title = "Warning"
            message = "The source file has been modified externally. Do you want to reload it?"
            confirmText = "Reload"
            dismissText = "Cancel"
            onDismiss = { showWarningDialog = false }
            this@EditorManager.warningDialogProperties.onConfirm = {
                getFileInstance()?.apply {
                    lastModified = File(filePath).lastModified()
                    onConfirm()
                    requireSave = false
                }
                requireSaveCurrentFile = false
                showWarningDialog = false
            }
            showWarningDialog = true
        }
    }

    fun getFileInstance(
        filePath: String = activeFilePath,
        bringToTop: Boolean = false
    ): FileInstance? {
        val index = fileInstanceList.indexOfFirst { it.filePath == filePath }
        if (index == -1) return null

        val instance = fileInstanceList[index]
        if (bringToTop) {
            fileInstanceList.removeAt(index)
            fileInstanceList.add(0, instance)
        }

        return instance
    }

    fun showSaveFileBeforeClose(fileInstance: FileInstance) {
        warningDialogProperties.apply {
            title = "Warning"
            message = "Do you want to save changes before closing?"
            confirmText = "Save"
            dismissText = "Discard"
            onDismiss = {
                fileInstanceList.remove(fileInstance)
                showWarningDialog = false
            }
            onConfirm = {
                save(
                    onSaved = {
                        fileInstanceList.remove(fileInstance)
                    },
                    onFailed = { /* Show error */ }
                )
                showWarningDialog = false
            }
            showWarningDialog = true
        }
    }

    fun save(onSaved: () -> Unit, onFailed: () -> Unit) {
        isSaving = true
        scope.launch {
            try {
                getFileInstance()?.let { instance ->
                    val file = File(instance.filePath)
                    file.writeText(instance.content.toString())
                    instance.lastModified = file.lastModified()
                    instance.requireSave = false
                    requireSaveCurrentFile = false
                    withContext(Dispatchers.Main) {
                        onSaved()
                        isSaving = false
                    }
                } ?: withContext(Dispatchers.Main) {
                    onFailed()
                    isSaving = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onFailed()
                    isSaving = false
                }
            }
        }
    }

    fun switchActiveFileTo(
        fileInstance: FileInstance,
        codeEditor: CodeEditor,
        onContentReady: (content: Content, text: String, isSourceFileChanged: Boolean) -> Unit
    ) {
        val file = File(fileInstance.filePath)
        if (!file.exists() || !file.isFile) {
            fileInstanceList.remove(fileInstance)
            // Show error toast
        }

        activeFilePath = fileInstance.filePath
        analyseFile()
        setLanguage(codeEditor)

        readActiveFileContent { content, text, isSourceFileChanged ->
            onContentReady(content, text, isSourceFileChanged)
        }
    }

    fun readActiveFileContent(
        onContentReady: (
            content: Content,
            text: String,
            isSourceFileChanged: Boolean
        ) -> Unit
    ) {
        analyseFile()

        scope.launch {
            isReading = true

            val file = File(activeFilePath)
            val text = file.readText()
            val fileInstance = getFileInstance(bringToTop = true)

            if (fileInstance != null) {
                val isSourceChanged = fileInstance.lastModified != file.lastModified()
                val isUnsaved = fileInstance.content.toString() != text

                fileInstance.requireSave = isSourceChanged || isUnsaved
                requireSaveCurrentFile = fileInstance.requireSave

                withContext(Dispatchers.Main) {
                    onContentReady(fileInstance.content, text, isSourceChanged)
                }
            } else {
                val content = Content(text)
                val newFileInstance = FileInstance(activeFilePath, content, file.lastModified())
                fileInstanceList.add(0, newFileInstance)

                withContext(Dispatchers.Main) {
                    onContentReady(content, text, false)
                }
            }

            isReading = false
        }
    }

    fun openTextEditor(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            // Show error
            return
        }

        activeFilePath = getFileInstance(filePath)?.filePath ?: filePath
        context.startActivity(Intent(context, RpyEditorActivityNew::class.java).apply {
            putExtra(RpyEditorActivityNew.EXTRA_FILE_PATH, filePath)
        })
    }

    private fun analyseFile() {
        val file = File(activeFilePath)
        activityTitle = file.name
        activitySubtitle = file.parent ?: ""
    }

    private fun setLanguage(codeEditor: CodeEditor) {
        // Apply Ren'Py TextMate language
        initializeTextMateIfNeeded()
        try {
            val language = TextMateLanguage.create("source.renpy", true)
            codeEditor.setEditorLanguage(language)
            codeEditor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeTextMateIfNeeded() {
        if (textMateInitialized) return

        try {
            FileProviderRegistry.getInstance().addFileProvider(
                AssetsFileResolver(context.assets)
            )

            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")

            val themeName = "renpy-dark"
            val themeAssetsPath = "textmate/renpy-dark-theme.json"
            val themeModel = ThemeModel(
                IThemeSource.fromInputStream(
                    FileProviderRegistry.getInstance().tryGetInputStream(themeAssetsPath),
                    themeAssetsPath,
                    null
                ),
                themeName
            )
            themeModel.isDark = true
            ThemeRegistry.getInstance().loadTheme(themeModel)
            ThemeRegistry.getInstance().setTheme(themeName)

            textMateInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFormattedCursorPosition(codeEditor: CodeEditor) = buildString {
        val cursor = codeEditor.cursor

        append("Ln ${cursor.leftLine + 1}, Col ${cursor.leftColumn + 1}")

        if (cursor.isSelected) {
            val selectionCount = cursor.right - cursor.left
            append(" ($selectionCount)")
        }

        val searcher = codeEditor.searcher
        if (searcher.hasQuery()) {
            val idx = searcher.currentMatchedPositionIndex
            append(" | ")
            if (idx == -1) {
                append("${searcher.matchedPositionCount} matches")
            } else {
                append("${idx + 1}/${searcher.matchedPositionCount}")
            }
        }
    }

    private fun selectionChangeListener(codeEditor: CodeEditor) {
        activitySubtitle = getFormattedCursorPosition(codeEditor)
    }

    fun createCodeEditorView(): CodeEditor {
        return CodeEditor(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            props.apply {
                symbolPairAutoCompletion = true
                deleteEmptyLineFast = true
                deleteMultiSpaces = -1
                autoIndent = true
                boldMatchingDelimiters = false
                formatPastedText = true
                isWordwrap = false
            }

            isLineNumberEnabled = true
            getComponent(EditorAutoCompletion::class.java).isEnabled = false
            getComponent(Magnifier::class.java).isEnabled = true

            typefaceText = Typeface.MONOSPACE

            setLanguage(this)

            subscribeAlways<SelectionChangeEvent> { selectionChangeListener(this) }
            subscribeAlways<PublishSearchResultEvent> { selectionChangeListener(this) }
            subscribeAlways<ContentChangeEvent> {
                getFileInstance()?.let {
                    if (!it.requireSave && !isReading) {
                        it.requireSave = true
                        requireSaveCurrentFile = true
                    }
                }
                canUndo = canUndo()
                canRedo = canRedo()
            }
        }
    }

    class RecentFileDialog {
        var showRecentFileDialog by mutableStateOf(false)

        fun getRecentFiles(editorManager: EditorManager): SnapshotStateList<FileInstance> {
            editorManager.fileInstanceList.removeIf { instance ->
                !File(instance.filePath).exists()
            }
            return editorManager.fileInstanceList
        }
    }

    data class FileInstance(
        val filePath: String,
        var content: Content,
        var lastModified: Long,
        var requireSave: Boolean = false,
        val searcher: Searcher = Searcher()
    ) {
        val fileName: String
            get() = File(filePath).name
    }
}
