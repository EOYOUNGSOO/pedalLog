package app.pedallog.android.ui.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.component.PedalCard
import app.pedallog.android.ui.component.PedalDivider
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalErrorBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg

@Composable
fun TemplateListScreen(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: TemplateListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites = uiState.templates.filter { it.isFavorite }
    val normalTemplates = uiState.templates.filterNot { it.isFavorite }

    Scaffold(
        topBar = { PedalAppBar(title = "코스 템플릿") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = PedalYellow,
                contentColor = PedalBgDark
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "템플릿 추가")
            }
        },
        containerColor = PedalBgDark
    ) { padding ->
        if (uiState.templates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "등록된 코스가 없습니다\n+ 버튼으로 추가하세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PedalTextMuted,
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            viewModel.updateSortOrder(uiState.templates.sortedBy { it.templateName })
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "이름순 정렬",
                            tint = PedalYellow
                        )
                    }
                }
            }

            if (favorites.isNotEmpty()) {
                item { TemplateSectionHeader(title = "⭐ 즐겨찾기") }
                items(favorites, key = { it.id }) { template ->
                    TemplateSwipeItem(
                        onDelete = { viewModel.deleteTemplate(template.id) }
                    ) {
                        TemplateListItem(
                            template = template,
                            onClick = { onNavigateToEdit(template.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(template.id) },
                            onMoveUp = { moveTemplate(template.id, uiState.templates, -1, viewModel) },
                            onMoveDown = { moveTemplate(template.id, uiState.templates, +1, viewModel) }
                        )
                    }
                }
            }

            if (normalTemplates.isNotEmpty()) {
                item { TemplateSectionHeader(title = "전체 코스") }
                items(normalTemplates, key = { it.id }) { template ->
                    TemplateSwipeItem(
                        onDelete = { viewModel.deleteTemplate(template.id) }
                    ) {
                        TemplateListItem(
                            template = template,
                            onClick = { onNavigateToEdit(template.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(template.id) },
                            onMoveUp = { moveTemplate(template.id, uiState.templates, -1, viewModel) },
                            onMoveDown = { moveTemplate(template.id, uiState.templates, +1, viewModel) }
                        )
                    }
                }
            }
        }
    }
}

private fun moveTemplate(
    templateId: Long,
    currentList: List<RidingTemplateEntity>,
    direction: Int,
    viewModel: TemplateListViewModel
) {
    val fromIndex = currentList.indexOfFirst { it.id == templateId }
    if (fromIndex == -1) return
    val toIndex = fromIndex + direction
    if (toIndex !in currentList.indices) return

    val mutable = currentList.toMutableList()
    val temp = mutable[fromIndex]
    mutable[fromIndex] = mutable[toIndex]
    mutable[toIndex] = temp
    viewModel.updateSortOrder(mutable)
}

@Composable
private fun TemplateSectionHeader(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = PedalYellow,
            fontWeight = FontWeight.Bold
        )
        PedalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateSwipeItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PedalErrorBg)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = PedalError
                )
            }
        },
        content = { content() }
    )
}

@Composable
private fun TemplateListItem(
    template: RidingTemplateEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    PedalCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = template.templateName,
                    style = MaterialTheme.typography.titleMedium,
                    color = PedalTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                val routeText = listOfNotNull(template.departure, template.destination)
                    .filter { it.isNotBlank() }
                    .joinToString(" → ")
                    .ifBlank { "경로 정보 없음" }
                Text(
                    text = routeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = PedalTextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "위로", tint = PedalTextMuted)
                }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "아래로", tint = PedalTextMuted)
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = if (template.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "즐겨찾기",
                        tint = if (template.isFavorite) PedalYellow else PedalTextMuted
                    )
                }
            }
        }
    }
}
