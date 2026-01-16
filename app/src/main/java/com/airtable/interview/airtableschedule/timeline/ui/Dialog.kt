package com.airtable.interview.airtableschedule.timeline.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.airtable.interview.airtableschedule.R
import com.airtable.interview.airtableschedule.models.Event


@Composable
fun EventDetailsDialog(
    event: Event,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        title = { Text(event.name) },
        text = {
            Column {
                Text("${stringResource(R.string.start)}: ${event.startDate}")
                Text("${stringResource(R.string.end)}: ${event.endDate}")
            }
        }
    )
}