package com.faigenbloom.vikarobux

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import com.faigenbloom.vikarobux.models.Item

@Composable
fun App(){
    val viewModel = remember { ItemsViewModel(RemoteRepository()) }
    MainScreen(viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(repository: ItemsViewModel) {
    val focusManager = LocalFocusManager.current

    val items by repository.items.collectAsState()
    val hintItems by repository.hintItems.collectAsState()
    val total by repository.total.collectAsState()

    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var showHints by remember { mutableStateOf(false) }

    var showUndoDialog by remember { mutableStateOf(false) }
    var lastDone: Item? by remember { mutableStateOf(null) }

    val qtyFocus = remember { FocusRequester() }

    Scaffold(
        topBar = {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(32.dp))
                Text(
                    "Вики Robux Банк",
                    style = MaterialTheme.typography.headlineMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Накопилось: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "$total",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

        },
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Input card
            Card(
                modifier = Modifier,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { showHints = it.isFocused },
                            label = { Text("Описание") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter(Char::isDigit) },
                            modifier = Modifier
                                .width(96.dp)
                                .focusRequester(qtyFocus),
                            label = { Text("К-во") },
                            singleLine = true,

                        )
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = if (description.isNotBlank() && quantity.isNotBlank())
                                        Color(0xFF6C63FF)
                                    else
                                        Color(0xFFBDBDBD),
                                    shape = CircleShape
                                )
                                .clickable(
                                    enabled = description.isNotBlank() && quantity.isNotBlank(),
                                    onClick = {
                                        repository.addItem(description, quantity.toIntOrNull() ?: 0)
                                        description = ""
                                        quantity = ""
                                        focusManager.clearFocus()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                        }


                    }
                    if (showHints && hintItems.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                hintItems.forEach { hint ->
                                    Text(
                                        text = hint,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                description = hint
                                                showHints = false
                                                qtyFocus.requestFocus()
                                            }
                                            .padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }


                    // Список
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 88.dp) // место под FAB
                    ) {
                        items(items, key = { it.id }) { item ->
                            ItemCard(
                                item = item,
                                onToggle = {
                                    if (item.isDone) {
                                        lastDone = item
                                        showUndoDialog = true
                                    } else {
                                        repository.markAsDone(item.id, true)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (showUndoDialog) {
        OkCancelDialog(
            onOk = {
                lastDone?.let { repository.markAsDone(it.id, false) }
                showUndoDialog = false
            },
            onCancel = { showUndoDialog = false }
        )
    }
}

@Composable
private fun ItemCard(
    item: Item,
    onToggle: () -> Unit
) {
    val container = if (item.isDone)
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.primaryContainer

    val content = if (item.isDone)
        MaterialTheme.colorScheme.onSurfaceVariant
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = container),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Дата
            Column(Modifier.weight(1f)) {
                Text(item.date, style = MaterialTheme.typography.labelMedium, color = content)
                Spacer(Modifier.height(2.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Бейдж количества
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .background(
                        color = if (item.isDone)
                            MaterialTheme.colorScheme.outlineVariant
                        else
                            MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("×${item.quantity}", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
