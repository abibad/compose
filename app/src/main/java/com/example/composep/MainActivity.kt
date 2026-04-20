package com.example.composep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composep.ui.theme.ComposePTheme

// Modelo de datos para una tarea
data class Task(
    val id: Int,
    val text: String,
    val isCompleted: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposePTheme {
                ToDoListApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoListApp() {
    val tasks = remember { mutableStateListOf<Task>() }
    var newTaskText by remember { mutableStateOf("") }
    var editingTaskId by remember { mutableStateOf<Int?>(null) }
    
    // Estado para manejar el diálogo de confirmación
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "GESTIÓN DE TAREAS",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6200EE), // Color Morado muy vibrante
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFEEEEEE)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("Escribe una nueva tarea...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                val nextId = (tasks.maxOfOrNull { it.id } ?: -1) + 1
                                tasks.add(Task(nextId, newTaskText))
                                newTaskText = ""
                            }
                        },
                        enabled = newTaskText.isNotBlank(),
                        modifier = Modifier
                            .background(
                                color = if (newTaskText.isNotBlank()) Color(0xFF6200EE) else Color.Gray,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        isEditing = editingTaskId == task.id,
                        onDelete = { taskToDelete = task }, // Abrir confirmación
                        onToggleComplete = {
                            val index = tasks.indexOf(task)
                            if (index != -1) {
                                tasks[index] = task.copy(isCompleted = !task.isCompleted)
                            }
                        },
                        onEdit = { editingTaskId = task.id },
                        onSaveEdit = { newText ->
                            val index = tasks.indexOf(task)
                            if (index != -1) {
                                tasks[index] = task.copy(text = newText)
                            }
                            editingTaskId = null
                        },
                        onCancelEdit = { editingTaskId = null }
                    )
                }
            }
        }

        // Diálogo de Confirmación para Eliminar
        taskToDelete?.let { task ->
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro de que quieres eliminar la tarea: \"${task.text}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tasks.remove(task)
                            taskToDelete = null
                        }
                    ) {
                        Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun TaskItemRow(
    task: Task,
    isEditing: Boolean,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit
) {
    var editedText by remember { mutableStateOf(task.text) }

    AnimatedVisibility(
        visible = true,
        exit = fadeOut() + shrinkVertically(animationSpec = spring())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (task.isCompleted) Color(0xFFC8E6C9) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E7D32))
                )

                if (isEditing) {
                    TextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Row {
                        IconButton(onClick = { onSaveEdit(editedText) }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
                        }
                        IconButton(onClick = onCancelEdit) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Red)
                        }
                    }
                } else {
                    Text(
                        text = task.text,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (task.isCompleted) Color.DarkGray else Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ComposePTheme {
        ToDoListApp()
    }
}
