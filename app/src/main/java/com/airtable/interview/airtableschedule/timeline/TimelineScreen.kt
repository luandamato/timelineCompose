package com.airtable.interview.airtableschedule.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.Event

/**
 * A screen that displays a timeline of events.
 */
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimelineView(uiState.events)
}

/**
 * A view that displays a list of events in swimlanes format.
 * TODO: Replace the exiting list with Swimlanes
 *
 * @param events The list of events to display.
 */
@Composable
private fun TimelineView(
    events: List<Event>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        events.forEach {
            EventView(event = it)
        }
    }
}

/**
 * Single event view.
 * TODO: This needs to be updated as needed
 */
@Composable
private fun EventView(event: Event) {
    Column(verticalArrangement = Arrangement.SpaceBetween) {
        Text(text = event.name)
        Text(text = event.startDate.toString())
        HorizontalDivider()
    }
}
