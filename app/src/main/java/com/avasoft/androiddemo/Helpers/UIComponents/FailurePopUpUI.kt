package com.avasoft.androiddemo.Helpers.UIComponents

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun FailurePopUp(label: String, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = "Cancel")
            }
        },
        title = { Text(text = "Oops !") },
        text = { Text(text = label) }
    )
}