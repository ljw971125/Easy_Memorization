package com.example.easy_memorization

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.easy_memorization.ui.theme.onSuccessContainer
import com.example.easy_memorization.ui.theme.success
import com.example.easy_memorization.ui.theme.successContainer

/**
 * Pure presentation logic for quiz option buttons: which visual style an option
 * gets based on selection/correctness. Kept free of Compose so it is unit-testable.
 */
enum class QuizOptionStyle { DEFAULT, CORRECT, WRONG }

fun resolveQuizOptionStyle(isSelected: Boolean, isCorrect: Boolean): QuizOptionStyle =
    when {
        isSelected && isCorrect -> QuizOptionStyle.CORRECT
        isSelected && !isCorrect -> QuizOptionStyle.WRONG
        else -> QuizOptionStyle.DEFAULT
    }

@Composable
private fun quizOptionButtonColors(style: QuizOptionStyle): ButtonColors =
    when (style) {
        QuizOptionStyle.CORRECT -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.successContainer,
            contentColor = MaterialTheme.colorScheme.onSuccessContainer
        )
        QuizOptionStyle.WRONG -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        QuizOptionStyle.DEFAULT -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (initialQuestion == null) "문제 등록" else "문제 수정",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("문제") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option1,
            onValueChange = { option1 = it },
            label = { Text("보기 1") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option2,
            onValueChange = { option2 = it },
            label = { Text("보기 2") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option3,
            onValueChange = { option3 = it },
            label = { Text("보기 3") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = option4,
            onValueChange = { option4 = it },
            label = { Text("보기 4") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = explanation,
            onValueChange = { explanation = it },
            label = { Text("해설 (오답 시 표시)") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "정답 선택",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0..3).forEach { index ->
                FilterChip(
                    selected = correctAnswerIndex == index,
                    onClick = { correctAnswerIndex = index },
                    label = { Text("${index + 1}") }
                )
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(top = 8.dp)
        ) {
            Text(if (initialQuestion == null) "추가하기" else "수정완료")
        }
        Spacer(modifier = Modifier.height(8.dp))
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
    var showSample by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "문제 세트 가져오기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "JSON 파일에서 문제 세트를 불러오세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                    onClick = onImportClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("파일에서 세트 불러오기 (JSON)")
                }
                TextButton(
                    onClick = { showSample = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("JSON 예시 보기")
                }
            }
        }

        if (showSample) {
            AlertDialog(
                onDismissRequest = { showSample = false },
                confirmButton = {
                    TextButton(onClick = { showSample = false }) {
                        Text("닫기")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(SAMPLE_IMPORT_JSON))
                            showSample = false
                        }
                    ) {
                        Text("복사")
                    }
                },
                title = { Text("JSON 예시") },
                text = {
                    Column {
                        Text(
                            "이 형식으로 파일을 만들어 가져오세요. (기존 형식도 계속 지원됩니다)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            SAMPLE_IMPORT_JSON,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLowest,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            )
        }

        if (sets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "등록된 문제 세트가 없습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "JSON으로 가져오거나 직접 등록해 보세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sets) { set ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                set.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${set.questions.size}문제",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = { onPlay(set) },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("풀기")
                                }
                                OutlinedButton(
                                    onClick = { onReview(set) },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("복습")
                                }
                                IconButton(onClick = { onDelete(set) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (shuffledQuestions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "세트에 문제가 없습니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onFinish,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("목록으로 돌아가기")
                    }
                }
            }
            return@Box
        }

        if (currentIndex >= shuffledQuestions.size) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "축하합니다!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "모든 문제를 풀었습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onFinish,
                        shape = RoundedCornerShape(16.dp)
                    ) {
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFinish) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Quiz"
                    )
                }
                Text(
                    text = "${questionSet.title} - 문제 ${currentIndex + 1} / ${shuffledQuestions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            LinearProgressIndicator(
                progress = { (currentIndex + 1) / shuffledQuestions.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = currentQuestion.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentQuestion.options.forEachIndexed { index, option ->
                            if (option.isNotBlank()) {
                                val style = resolveQuizOptionStyle(
                                    isSelected = selectedIndex == index,
                                    isCorrect = index == currentQuestion.correctAnswerIndex
                                )
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
                                    shape = RoundedCornerShape(16.dp),
                                    colors = quizOptionButtonColors(style),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 56.dp)
                                ) {
                                    if (style == QuizOptionStyle.CORRECT) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text(text = option)
                                }
                            }
                        }
                    }
                }
            }

            if (showExplanation) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "오답입니다.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentQuestion.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "해설: ${currentQuestion.explanation}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                currentIndex++
                                selectedIndex = -1
                                showExplanation = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("다음 문제")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(questionSet.questions) { question ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Q. ${question.text}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        question.options.forEachIndexed { index, option ->
                            if (option.isNotBlank()) {
                                val isCorrect = index == question.correctAnswerIndex
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    if (isCorrect) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "정답",
                                            tint = MaterialTheme.colorScheme.success,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                    } else {
                                        Spacer(Modifier.width(24.dp))
                                    }
                                    Text(
                                        text = "${index + 1}. $option",
                                        color = if (isCorrect) MaterialTheme.colorScheme.success
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        if (question.explanation.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Text(
                                text = "해설: ${question.explanation}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            Text(
                "등록된 문제가 없습니다.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions) { question ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(question.text, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "정답: ${question.correctAnswerIndex + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onEdit(question) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { onDelete(question) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
