package com.example.easy_memorization

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuestionEntryScreen(
    initialQuestion: Question? = null,
    onSave: (Question) -> Unit
) {
    var questionText by remember(initialQuestion) { mutableStateOf(initialQuestion?.text ?: "") }
    var option1 by remember(initialQuestion) { mutableStateOf(initialQuestion?.options?.getOrNull(0) ?: "") }
    var option2 by remember(initialQuestion) { mutableStateOf(initialQuestion?.options?.getOrNull(1) ?: "") }
    var option3 by remember(initialQuestion) { mutableStateOf(initialQuestion?.options?.getOrNull(2) ?: "") }
    var option4 by remember(initialQuestion) { mutableStateOf(initialQuestion?.options?.getOrNull(3) ?: "") }
    var explanation by remember(initialQuestion) { mutableStateOf(initialQuestion?.explanation ?: "") }
    var correctAnswerIndex by remember(initialQuestion) { mutableIntStateOf(initialQuestion?.correctAnswerIndex ?: 0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (initialQuestion == null) "문제 등록" else "문제 수정",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("문제") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option1,
            onValueChange = { option1 = it },
            label = { Text("보기 1") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option2,
            onValueChange = { option2 = it },
            label = { Text("보기 2") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option3,
            onValueChange = { option3 = it },
            label = { Text("보기 3") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option4,
            onValueChange = { option4 = it },
            label = { Text("보기 4") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = explanation,
            onValueChange = { explanation = it },
            label = { Text("해설 (오답 시 표시)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("정답 선택")
        Row(verticalAlignment = Alignment.CenterVertically) {
            (0..3).forEach { index ->
                RadioButton(
                    selected = correctAnswerIndex == index,
                    onClick = { correctAnswerIndex = index }
                )
                Text("${index + 1}")
            }
        }

        Button(
            onClick = {
                if (questionText.isNotBlank() && option1.isNotBlank()) {
                    onSave(
                        Question(
                            id = initialQuestion?.id ?: System.currentTimeMillis(),
                            text = questionText,
                            options = listOf(option1, option2, option3, option4),
                            correctAnswerIndex = correctAnswerIndex,
                            explanation = explanation
                        )
                    )
                    if (initialQuestion == null) {
                        questionText = ""
                        option1 = ""
                        option2 = ""
                        option3 = ""
                        option4 = ""
                        explanation = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (initialQuestion == null) "추가하기" else "수정완료")
        }
    }
}

@Composable
fun SetListScreen(
    sets: List<QuestionSet>,
    onPlay: (QuestionSet) -> Unit,
    onReview: (QuestionSet) -> Unit,
    onDelete: (QuestionSet) -> Unit,
    onImportClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("파일에서 세트 불러오기 (JSON)")
        }

        if (sets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 문제 세트가 없습니다.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sets) { set ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(set.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("문제 수: ${set.questions.size}", style = MaterialTheme.typography.bodyMedium)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onPlay(set) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("풀기")
                                }
                                OutlinedButton(
                                    onClick = { onReview(set) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("복습")
                                }
                                IconButton(onClick = { onDelete(set) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizScreen(questionSet: QuestionSet, onFinish: () -> Unit) {
    BackHandler {
        onFinish()
    }

    val shuffledQuestions = remember(questionSet) { questionSet.questions.shuffled() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var showExplanation by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (shuffledQuestions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("세트에 문제가 없습니다.", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onFinish) {
                        Text("목록으로 돌아가기")
                    }
                }
            }
            return@Box
        }

        if (currentIndex >= shuffledQuestions.size) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("축하합니다!", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("모든 문제를 풀었습니다.", style = MaterialTheme.typography.bodyLarge, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onFinish) {
                        Text("목록으로 돌아가기")
                    }
                }
            }
            return@Box
        }

        val currentQuestion = shuffledQuestions[currentIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFinish) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Quiz",
                        tint = Color.White
                    )
                }
                Text(
                    text = "${questionSet.title} - 문제 ${currentIndex + 1} / ${shuffledQuestions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentQuestion.text,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    currentQuestion.options.forEachIndexed { index, option ->
                        if (option.isNotBlank()) {
                            val buttonColor = if (selectedIndex == index) {
                                if (index == currentQuestion.correctAnswerIndex) Color(0xFF2E7D32) else Color(0xFFC62828)
                            } else {
                                Color(0xFF333333)
                            }

                            Button(
                                onClick = {
                                    if (!showExplanation) {
                                        selectedIndex = index
                                        if (index == currentQuestion.correctAnswerIndex) {
                                            currentIndex++
                                            selectedIndex = -1
                                            showExplanation = false
                                        } else {
                                            showExplanation = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(text = option, color = Color.White)
                            }
                        }
                    }
                }
            }

            if (showExplanation) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3E1A1A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "오답입니다.",
                            color = Color(0xFFFF6B6B),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentQuestion.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "해설: ${currentQuestion.explanation}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                currentIndex++
                                selectedIndex = -1
                                showExplanation = false
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White
                            )
                        ) {
                            Text("다음 문제", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(questionSet: QuestionSet, onBack: () -> Unit) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${questionSet.title} 복습") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(questionSet.questions) { question ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Q: ${question.text}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        question.options.forEachIndexed { index, option ->
                            if (option.isNotBlank()) {
                                Text(
                                    text = "${index + 1}. $option",
                                    color = if (index == question.correctAnswerIndex) Color(0xFF2E7D32) else Color.Unspecified,
                                    fontWeight = if (index == question.correctAnswerIndex) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        if (question.explanation.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(text = "해설: ${question.explanation}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionListScreen(
    questions: List<Question>,
    onEdit: (Question) -> Unit,
    onDelete: (Question) -> Unit
) {
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("등록된 문제가 없습니다.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions) { question ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(question.text, style = MaterialTheme.typography.titleMedium)
                            Text("정답: ${question.correctAnswerIndex + 1}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onEdit(question) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { onDelete(question) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
