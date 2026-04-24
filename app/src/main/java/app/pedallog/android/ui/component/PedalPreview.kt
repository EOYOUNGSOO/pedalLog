package app.pedallog.android.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.pedallog.android.ui.theme.PedalLogTheme

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DesignSystemPreview() {
    PedalLogTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PedalStatCard("70.1", "km", "총 거리", Modifier.weight(1f))
                PedalStatCard("165", "분", "총 시간", Modifier.weight(1f))
                PedalStatCard("3", "회", "라이딩", Modifier.weight(1f))
                PedalStatCard("900", "kcal", "칼로리", Modifier.weight(1f))
            }

            PedalPrimaryButton("Notion에 등록", onClick = {})
            PedalOutlineButton("취소", onClick = {})
            PedalDangerButton("삭제", onClick = {})

            var text by remember { mutableStateOf("뚝섬라이딩") }
            PedalTextField(
                value = text,
                onValueChange = { text = it },
                label = "코스명",
                isRequired = true
            )
            PedalAutoFillField(label = "출발지", value = "왕숙천교")

            PedalStatusCard("Notion 등록 완료!", isSuccess = true)
            PedalStatusCard("연결 오류 - 재시도해주세요", isSuccess = false)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PedalPhaseBadge("P1")
                PedalPhaseBadge("P2")
                PedalPhaseBadge("P3")
                PedalStatusChip(isSuccess = true)
                PedalStatusChip(isSuccess = false)
            }
        }
    }
}
