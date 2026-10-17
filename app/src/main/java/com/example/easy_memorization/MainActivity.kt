package com.example.easy_memorization

import android.os.Bundle
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.easy_memorization.ui.theme.Easy_MemorizationTheme
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Easy_MemorizationTheme {
                val context = LocalContext.current
                val questionSets = remember { mutableStateListOf<QuestionSet>() }
                var currentNavIndex by remember { mutableStateOf(0) } // 0: Home (Sets), 1: Add Question
                
                var activeQuizSet by remember { mutableStateOf<QuestionSet?>(null) }
                var activeReviewSet by remember { mutableStateOf<QuestionSet?>(null) }
                
                // 앱 시작 시 저장된 문제 세트 불러오기
                LaunchedEffect(Unit) {
                    val savedSets = QuestionRepository.loadQuestionSets(context)
                    questionSets.clear()
                    questionSets.addAll(savedSets)
                }

                // 수동 등록을 위한 기본 세트
                val defaultSetName = "수동 등록 문제"

                var importError by remember { mutableStateOf<String?>(null) }

                // ContentResolver의 DISPLAY_NAME을 기본 세트 제목으로 사용하고,
                // 없을 때만 기존의 uri.path 방식으로 대체한다.
                fun resolveImportTitle(uri: Uri): String {
                    try {
                        contentResolver.query(
                            uri,
                            arrayOf(OpenableColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (index >= 0) {
                                    val displayName = cursor.getString(index)
                                    if (!displayName.isNullOrBlank()) {
                                        return stripJsonExtension(displayName)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                    return uri.path?.substringAfterLast("/") ?: "새 문제 세트"
                }

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        try {
                            contentResolver.openInputStream(it)?.use { inputStream ->
                                val reader = BufferedReader(InputStreamReader(inputStream))
                                val content = reader.readText()
                                val defaultTitle = resolveImportTitle(it)
                                // 기존 엄격 파서를 먼저 시도하고, 실패하면 관대한 파서로 대체한다.
                                val importedSets = parseImportedSets(content, defaultTitle)
                                importedSets.forEach { newSet ->
                                    questionSets.add(
                                        newSet.copy(
                                            questions = newSet.questions.map { q ->
                                                q.copy(id = System.currentTimeMillis() + (0..1000000).random())
                                            }
                                        )
                                    )
                                }
                                QuestionRepository.saveQuestionSets(context, questionSets)
                            }
                        } catch (e: Exception) {
                            importError = "가져오기에 실패했습니다: ${e.message}"
                        }
                    }
                }

                if (activeQuizSet != null) {
                    QuizScreen(
                        questionSet = activeQuizSet!!,
                        onFinish = { activeQuizSet = null }
                    )
                } else if (activeReviewSet != null) {
                    ReviewScreen(
                        questionSet = activeReviewSet!!,
                        onBack = { activeReviewSet = null }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentNavIndex == 0,
                                    onClick = { currentNavIndex = 0 },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("세트") }
                                )
                                NavigationBarItem(
                                    selected = currentNavIndex == 1,
                                    onClick = { currentNavIndex = 1 },
                                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                                    label = { Text("직접등록") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentNavIndex) {
                                0 -> SetListScreen(
                                    sets = questionSets,
                                    onPlay = { activeQuizSet = it },
                                    onReview = { activeReviewSet = it },
                                    onDelete = { set ->
                                        questionSets.remove(set)
                                        QuestionRepository.saveQuestionSets(context, questionSets)
                                    },
                                    onImportClick = { filePickerLauncher.launch("application/json") }
                                )
                                1 -> QuestionEntryScreen(
                                    onSave = { newQuestion ->
                                        val existingSetIndex = questionSets.indexOfFirst { it.title == defaultSetName }
                                        if (existingSetIndex != -1) {
                                            val updatedSet = questionSets[existingSetIndex].copy(
                                                questions = questionSets[existingSetIndex].questions + newQuestion
                                            )
                                            questionSets[existingSetIndex] = updatedSet
                                        } else {
                                            questionSets.add(
                                                QuestionSet(
                                                    title = defaultSetName,
                                                    questions = listOf(newQuestion)
                                                )
                                            )
                                        }
                                        QuestionRepository.saveQuestionSets(context, questionSets)
                                        currentNavIndex = 0 // 등록 후 목록으로
                                    }
                                )
                            }
                        }
                    }
                }

                if (importError != null) {
                    AlertDialog(
                        onDismissRequest = { importError = null },
                        confirmButton = {
                            TextButton(onClick = { importError = null }) {
                                Text("확인")
                            }
                        },
                        title = { Text("가져오기 실패") },
                        text = { Text(importError ?: "") }
                    )
                }
            }
        }
    }
}
