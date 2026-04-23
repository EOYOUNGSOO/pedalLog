package app.pedalLog.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedalLog.android.data.db.AppDatabase
import app.pedalLog.android.data.repository.TemplateRepository
import app.pedalLog.android.ui.confirm.ConfirmScreen
import app.pedalLog.android.ui.confirm.ConfirmViewModel
import app.pedalLog.android.ui.confirm.ConfirmViewModelFactory
import app.pedalLog.android.ui.home.HomeScreen
import app.pedalLog.android.ui.template.TemplateEditScreen
import app.pedalLog.android.ui.template.TemplateScreen
import app.pedalLog.android.ui.template.TemplateViewModel
import app.pedalLog.android.ui.template.TemplateViewModelFactory

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        val db = AppDatabase.getInstance(this)
        TemplateRepository(db.ridingTemplateDao(), db.bikeTypeDao())
    }

    private val templateViewModel: TemplateViewModel by viewModels {
        TemplateViewModelFactory(repository)
    }
    private val confirmViewModel: ConfirmViewModel by viewModels {
        ConfirmViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var route by remember { mutableStateOf("home") }
            var editingId by remember { mutableStateOf<Long?>(null) }
            val templates = templateViewModel.templates.collectAsStateWithLifecycle().value
            val bikeTypes = templateViewModel.bikeTypes.collectAsStateWithLifecycle().value

            when (route) {
                "home" -> HomeScreen(
                    onMoveToConfirm = { route = "confirm" },
                    onMoveToTemplates = { route = "template" }
                )
                "confirm" -> ConfirmScreen(
                    viewModel = confirmViewModel,
                    onNavigateTemplateCreate = {
                        editingId = null
                        route = "templateEdit"
                    }
                )
                "template" -> TemplateScreen(
                    templates = templates,
                    onToggleFavorite = templateViewModel::toggleFavorite,
                    onDelete = templateViewModel::delete,
                    onEdit = {
                        editingId = it.id
                        route = "templateEdit"
                    },
                    onAdd = {
                        editingId = null
                        route = "templateEdit"
                    },
                    onReorder = templateViewModel::reorder
                )
                "templateEdit" -> TemplateEditScreen(
                    bikeTypes = bikeTypes,
                    initialValue = templates.firstOrNull { it.id == editingId },
                    onSave = {
                        templateViewModel.save(it)
                        route = "template"
                    },
                    onBack = { route = "template" }
                )
            }
        }
    }
}
