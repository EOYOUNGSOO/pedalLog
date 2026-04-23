package app.pedalLog.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onMoveToConfirm: () -> Unit, onMoveToTemplates: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("pedalLog Home (배너 광고 위치)")
        Button(onClick = onMoveToConfirm, modifier = Modifier.padding(top = 12.dp)) {
            Text("라이딩 확인")
        }
        Button(onClick = onMoveToTemplates, modifier = Modifier.padding(top = 8.dp)) {
            Text("템플릿 관리")
        }
    }
}
