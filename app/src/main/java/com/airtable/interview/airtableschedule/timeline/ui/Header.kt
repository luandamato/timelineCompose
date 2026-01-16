package com.airtable.interview.airtableschedule.timeline.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airtable.interview.airtableschedule.R
import com.airtable.interview.airtableschedule.timeline.CalendarViewMode


@Composable
fun CalendarHeader(
    selectedMode: CalendarViewMode,
    onModeChange: (CalendarViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CalendarViewMode.entries.forEach { mode ->
            val labelResId = when (mode) {
                CalendarViewMode.WEEK -> R.string.week
                CalendarViewMode.MONTH -> R.string.month
                CalendarViewMode.YEAR -> R.string.year
            }
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeChange(mode) },
                label = { Text(stringResource(labelResId)) }
            )
        }
    }
}

@Composable
fun PeriodNavigationHeader(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrevious) {
            Text("◀", style = MaterialTheme.typography.headlineMedium)
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(onClick = onNext) {
            Text("▶", style = MaterialTheme.typography.headlineMedium)
        }
    }
}