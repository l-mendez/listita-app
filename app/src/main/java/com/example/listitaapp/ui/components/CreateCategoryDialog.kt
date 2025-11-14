package com.example.listitaapp.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.listitaapp.R

@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    autoDismiss: Boolean = true
) {
    var categoryName by remember { mutableStateOf("") }

    AppFormDialog(
        title = stringResource(R.string.create_category),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.create_category),
        confirmEnabled = categoryName.isNotBlank(),
        onConfirm = {
            if (categoryName.isNotBlank()) {
                onCreate(categoryName)
                if (autoDismiss) {
                    onDismiss()
                }
            }
        }
    ) {
        AppTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = stringResource(R.string.category_name)
        )
    }
}
