package com.example.listitaapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.listitaapp.R
import com.example.listitaapp.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductDialog(
    categories: List<Category>,
    productName: String,
    onProductNameChange: (String) -> Unit,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    onCreate: (String, Long?) -> Unit,
    onRequestCreateCategory: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AppFormDialog(
        title = stringResource(R.string.create_product),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.create_product),
        confirmEnabled = productName.isNotBlank(),
        onConfirm = {
            if (productName.isNotBlank()) {
                onCreate(productName, selectedCategoryId)
                onDismiss()
            }
        }
    ) {
        AppTextField(
            value = productName,
            onValueChange = onProductNameChange,
            label = stringResource(R.string.product_name)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            AppTextField(
                value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = stringResource(R.string.category),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category.id)
                            expanded = false
                        }
                    )
                }
            }
        }
        TextButton(
            onClick = onRequestCreateCategory,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = stringResource(R.string.create_category))
        }
    }
}
