package app.pedallog.android.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.pedallog.android.ui.theme.PedalBgInput
import app.pedallog.android.ui.theme.PedalBorder
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalLogTheme
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalYellow

@Composable
fun PedalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val labelText = if (isRequired) "$label *" else label
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        placeholder = { Text(placeholder, color = PedalTextMuted) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PedalYellow,
            unfocusedBorderColor = PedalBorder,
            errorBorderColor = PedalError,
            focusedLabelColor = PedalYellow,
            unfocusedLabelColor = PedalTextMuted,
            focusedContainerColor = PedalBgInput,
            unfocusedContainerColor = PedalBgInput,
            cursorColor = PedalYellow,
            focusedTextColor = PedalTextPrimary,
            unfocusedTextColor = PedalTextPrimary
        ),
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = PedalError) }
        } else {
            null
        },
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun PedalAutoFillField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label, color = PedalSuccess) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PedalSuccess,
            unfocusedBorderColor = PedalSuccess,
            focusedContainerColor = PedalSuccessBg,
            unfocusedContainerColor = PedalSuccessBg,
            focusedTextColor = PedalTextPrimary,
            unfocusedTextColor = PedalTextPrimary
        ),
        trailingIcon = {
            Icon(
                Icons.Default.Build,
                contentDescription = "자동 채움",
                tint = PedalSuccess,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PedalTextFieldPreview() {
    val state = remember { mutableStateOf("뚝섬라이딩") }
    PedalLogTheme {
        PedalTextField(
            value = state.value,
            onValueChange = { state.value = it },
            label = "코스명",
            isRequired = true
        )
    }
}
