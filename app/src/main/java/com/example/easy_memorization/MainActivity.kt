package com.example.easy_memorization

import android.os.Bundle
import android.net.Uri
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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.easy_memorization.ui.theme.Easy_MemorizationTheme
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    private val json = Json { ignoreUnknownKeys = true }

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
                
                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        try {
                            contentResolver.openInputStream(it)?.use { inputStream ->
                                val reader = BufferedReader(InputStreamReader(inputStream))
                                val content = reader.readText()
                                val importedQuestions = json.decodeFromString<List<Question>>(content)
                                
                                // 파일명을 추출하여 세트 제목으로 사용 (간단하게 URI 마지막 부분 사용)
                                val fileName = it.path?.substringAfterLast("/") ?: "새 문제 세트"
                                
                                val newSet = QuestionSet(
                                    title = fileName,
                                    questions = importedQuestions.map { q ->
                                        q.copy(id = System.currentTimeMillis() + (0..1000000).random())
                                    }
                                )
                                questionSets.add(newSet)
                                QuestionRepository.saveQuestionSets(context, questionSets)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
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
            }
        }
    }
}
