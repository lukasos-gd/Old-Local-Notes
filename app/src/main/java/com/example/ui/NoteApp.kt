package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FileSyncHelper
import com.example.data.Note
import com.example.ui.theme.NoteColorsList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransmissionType {
    BACKUP,
    RESTORE
}

enum class TransmissionState {
    TRANSMITTING,
    SUCCESS,
    FAILED
}

data class TransmissionStatus(
    val type: TransmissionType,
    val state: TransmissionState,
    val noteCount: Int,
    val bytesTransferred: Long,
    val message: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteApp(
    viewModel: NoteViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var editingNote by remember { mutableStateOf<Note?>(null) }
    var isCreatingNote by remember { mutableStateOf(false) }
    var transmissionStatus by remember { mutableStateOf<TransmissionStatus?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Local Notes",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            triggerBackupTransmission(context, notes, coroutineScope) { status ->
                                transmissionStatus = status
                            }
                        },
                        modifier = Modifier.testTag("backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Back up Notes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            triggerRestoreTransmission(context, viewModel, coroutineScope) { status ->
                                transmissionStatus = status
                            }
                        },
                        modifier = Modifier.testTag("restore_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Restore Backup",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle color mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNote = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_note_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new Note"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                placeholder = { Text("Search title, category or contents...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Category Horizontal Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = (selectedCategory == category) ||
                            (selectedCategory.isNullOrBlank() && category == "All")

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.setSelectedCategory(if (category == "All") null else category)
                            }
                            .testTag("category_pill_$category"),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Notes List / Grid
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank() || (!selectedCategory.isNullOrBlank() && selectedCategory != "All")) {
                            "No matching notes found."
                        } else {
                            "No notes yet. Click lower button to add!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onCardClick = { editingNote = note },
                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Add/Edit Note
    if (isCreatingNote || editingNote != null) {
        val targetNote = editingNote
        ModalBottomSheet(
            onDismissRequest = {
                isCreatingNote = false
                editingNote = null
            },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NoteEditorContent(
                note = targetNote,
                onDismiss = {
                    isCreatingNote = false
                    editingNote = null
                },
                onSave = { updatedNote ->
                    viewModel.saveNote(updatedNote) {
                        isCreatingNote = false
                        editingNote = null
                    }
                }
            )
        }
    }

    // Custom Live Transmission Stream Dialog
    transmissionStatus?.let { status ->
        Dialog(onDismissRequest = {
            if (status.state != TransmissionState.TRANSMITTING) {
                transmissionStatus = null
            }
        }) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(if (status.state == TransmissionState.TRANSMITTING) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                when (status.state) {
                                    TransmissionState.TRANSMITTING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    TransmissionState.SUCCESS -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    TransmissionState.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                }
                            )
                    ) {
                        Icon(
                            imageVector = if (status.type == TransmissionType.BACKUP) Icons.Default.CloudUpload else Icons.Default.CloudDownload,
                            contentDescription = "Transmission Stream",
                            modifier = Modifier.size(36.dp),
                            tint = when (status.state) {
                                TransmissionState.TRANSMITTING -> MaterialTheme.colorScheme.primary
                                TransmissionState.SUCCESS -> MaterialTheme.colorScheme.secondary
                                TransmissionState.FAILED -> MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when (status.state) {
                            TransmissionState.TRANSMITTING -> "SYNCHRONIZING SECURE STREAM..."
                            TransmissionState.SUCCESS -> "SYNCHRONIZATION COMPLETED"
                            TransmissionState.FAILED -> "TRANSMISSION FAILED"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = when (status.state) {
                            TransmissionState.TRANSMITTING -> MaterialTheme.colorScheme.primary
                            TransmissionState.SUCCESS -> MaterialTheme.colorScheme.secondary
                            TransmissionState.FAILED -> MaterialTheme.colorScheme.error
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transmission Telemetry Metadata Block
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Transmitter Channel:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (status.type == TransmissionType.BACKUP) "UPLOAD (EXPORT)" else "DOWNLOAD (RESTORE)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Database Records:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${status.noteCount} Notes",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Data Byte Size:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (status.bytesTransferred > 0) "${status.bytesTransferred} B" else "Pending...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (status.state == TransmissionState.TRANSMITTING) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = status.message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    if (status.state != TransmissionState.TRANSMITTING) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { transmissionStatus = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status.state == TransmissionState.SUCCESS) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Acknowledge & Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = parseColorOrDefault(note.colorHex, MaterialTheme.colorScheme.surfaceVariant)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(note.updatedAt) { dateFormat.format(Date(note.updatedAt)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .testTag("note_card_${note.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = note.category.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Row {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("favorite_toggle_${note.id}")
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite Toggle",
                            tint = if (note.isFavorite) Color(0xFFFFC107) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = note.title.ifBlank { "Untitled Note" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun NoteEditorContent(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var colorHex by remember { mutableStateOf(note?.colorHex ?: "#242735") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (note == null) "New Note" else "Edit Note",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_editor_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close compose dialogue"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            placeholder = { Text("Untitled Note") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_title_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (e.g. Work, Ideas, Shopping)") },
            placeholder = { Text("Personal") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_category_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Select Card Color Accent",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(NoteColorsList) { color ->
                val hexString = String.format("#%06X", (0xFFFFFF and color.value.toInt()))
                val isSelected = colorHex.equals(hexString, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { colorHex = hexString }
                        .testTag("color_option_$hexString"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Text Editor") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Markdown Viewer") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Write content here (Markdown headers #, italics * and bolding ** supported...)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 320.dp)
                    .testTag("note_content_input"),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 320.dp)
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (content.isBlank()) {
                        Text(
                            text = "Nothing to preview. Content is empty.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        MarkdownText(
                            text = content,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val finalNote = Note(
                    id = note?.id ?: 0,
                    title = title.trim().ifBlank { "Untitled Note" },
                    content = content,
                    category = category.trim(),
                    colorHex = colorHex,
                    isFavorite = note?.isFavorite ?: false,
                    updatedAt = System.currentTimeMillis()
                )
                onSave(finalNote)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_note_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Save Changes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

fun triggerBackupTransmission(
    context: Context,
    notes: List<Note>,
    coroutineScope: CoroutineScope,
    onStatusChange: (TransmissionStatus?) -> Unit
) {
    coroutineScope.launch {
        onStatusChange(
            TransmissionStatus(
                type = TransmissionType.BACKUP,
                state = TransmissionState.TRANSMITTING,
                noteCount = notes.size,
                bytesTransferred = 0L,
                message = "Initiating handshake with storage framework..."
            )
        )
        delay(600)
        onStatusChange(
            TransmissionStatus(
                type = TransmissionType.BACKUP,
                state = TransmissionState.TRANSMITTING,
                noteCount = notes.size,
                bytesTransferred = 0L,
                message = "Room records into encrypted JSON stream packets..."
            )
        )
        delay(600)
        val file = FileSyncHelper.backupNotes(context, notes)
        if (file != null && file.exists()) {
            onStatusChange(
                TransmissionStatus(
                    type = TransmissionType.BACKUP,
                    state = TransmissionState.SUCCESS,
                    noteCount = notes.size,
                    bytesTransferred = file.length(),
                    message = "Transmission complete! Successfully dispatched and saved ${file.length()} bytes to ${file.name}"
                )
            )
        } else {
            onStatusChange(
                TransmissionStatus(
                    type = TransmissionType.BACKUP,
                    state = TransmissionState.FAILED,
                    noteCount = 0,
                    bytesTransferred = 0L,
                    message = "Handshake aborted! System unable to allocate or write payload to device storage."
                )
            )
        }
    }
}

fun triggerRestoreTransmission(
    context: Context,
    viewModel: NoteViewModel,
    coroutineScope: CoroutineScope,
    onStatusChange: (TransmissionStatus?) -> Unit
) {
    coroutineScope.launch {
        onStatusChange(
            TransmissionStatus(
                type = TransmissionType.RESTORE,
                state = TransmissionState.TRANSMITTING,
                noteCount = 0,
                bytesTransferred = 0L,
                message = "Searching for data backup document packets in local storage..."
            )
        )
        delay(600)
        val backupFile = FileSyncHelper.getBackupFile(context)
        if (backupFile != null && backupFile.exists() && backupFile.length() > 0) {
            onStatusChange(
                TransmissionStatus(
                    type = TransmissionType.RESTORE,
                    state = TransmissionState.TRANSMITTING,
                    noteCount = 0,
                    bytesTransferred = backupFile.length(),
                    message = "Found backup payload! Reading ${backupFile.length()} bytes from external storage..."
                )
            )
            delay(600)
            val restored = FileSyncHelper.restoreNotesFromBackup(context)
            if (restored.isNotEmpty()) {
                restored.forEach { note ->
                    viewModel.saveNote(note)
                }
                onStatusChange(
                    TransmissionStatus(
                        type = TransmissionType.RESTORE,
                        state = TransmissionState.SUCCESS,
                        noteCount = restored.size,
                        bytesTransferred = backupFile.length(),
                        message = "Parsing complete! Successfully restored ${restored.size} notes into Local Room database Cache."
                    )
                )
            } else {
                onStatusChange(
                    TransmissionStatus(
                        type = TransmissionType.RESTORE,
                        state = TransmissionState.FAILED,
                        noteCount = 0,
                        bytesTransferred = 0L,
                        message = "Inquiry failed! No valid backup file has been transmitted page-side; please generate a backup first."
                    )
                )
            }
        } else {
            onStatusChange(
                TransmissionStatus(
                    type = TransmissionType.RESTORE,
                    state = TransmissionState.FAILED,
                    noteCount = 0,
                    bytesTransferred = 0L,
                    message = "Inquiry failed! No valid backup file has been transmitted page-side; please generate a backup first."
                )
            )
        }
    }
}

private fun parseColorOrDefault(colorHex: String, defaultColor: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        defaultColor
    }
}
