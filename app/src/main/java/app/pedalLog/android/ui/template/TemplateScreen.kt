package app.pedalLog.android.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pedalLog.android.data.db.entity.RidingTemplateEntity
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    templates: List<RidingTemplateEntity>,
    onToggleFavorite: (RidingTemplateEntity) -> Unit,
    onDelete: (RidingTemplateEntity) -> Unit,
    onEdit: (RidingTemplateEntity) -> Unit,
    onAdd: () -> Unit,
    onReorder: (List<RidingTemplateEntity>) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<RidingTemplateEntity?>(null) }
    var localList by remember(templates) { mutableStateOf(templates) }
    val favoritesCount = templates.count { it.isFavorite }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            localList = localList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { _, _ -> onReorder(localList) }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("템플릿") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Edit, contentDescription = "새 템플릿")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .reorderable(reorderState)
                .detectReorderAfterLongPress(reorderState)
        ) {
            items(localList, key = { it.id }) { item ->
                if (!item.isFavorite && localList.indexOf(item) == favoritesCount) {
                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    Text("일반")
                }
                ReorderableItem(reorderState, key = item.id) {
                    SwipeToDismissBox(
                        state = androidx.compose.material3.rememberSwipeToDismissBoxState(
                            positionalThreshold = { it * 0.3f },
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    deleteTarget = item
                                    false
                                } else true
                            }
                        ),
                        backgroundContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "삭제")
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.courseName} | ${item.departure} -> ${item.destination}")
                            Row {
                                IconButton(onClick = { onToggleFavorite(item) }) {
                                    Icon(
                                        if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "즐겨찾기"
                                    )
                                }
                                IconButton(onClick = { onEdit(item) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "수정")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("삭제 확인") },
            text = { Text("선택한 템플릿을 삭제하시겠습니까?") },
            confirmButton = {
                IconButton(onClick = {
                    onDelete(deleteTarget!!)
                    deleteTarget = null
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제 확정")
                }
            },
            dismissButton = {
                IconButton(onClick = { deleteTarget = null }) {
                    Icon(Icons.Default.Edit, contentDescription = "취소")
                }
            }
        )
    }
}
