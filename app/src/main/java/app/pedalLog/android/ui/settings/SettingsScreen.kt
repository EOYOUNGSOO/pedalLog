package app.pedalLog.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val token = remember { mutableStateOf("") }
    val dbId = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = token.value,
            onValueChange = { token.value = it },
            label = { Text("Notion API 토큰") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = dbId.value,
            onValueChange = { dbId.value = it },
            label = { Text("Notion DB ID") }
        )
        Text("처리방침 링크 / 자전거 종류 관리 UI 추가 예정")
    }
}
