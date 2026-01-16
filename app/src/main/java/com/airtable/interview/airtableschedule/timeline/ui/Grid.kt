package com.airtable.interview.airtableschedule.timeline.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.airtable.interview.airtableschedule.R
import com.airtable.interview.airtableschedule.models.Event
import com.airtable.interview.airtableschedule.timeline.CalendarViewMode
import com.airtable.interview.airtableschedule.timeline.EventTrack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach


@Composable
fun TimelineGrid(
    dates: List<Date>,
    eventsWithTracks: List<EventTrack>,
    viewMode: CalendarViewMode,
    onEventClick: (Event) -> Unit
) {
    val maxTracks = remember(eventsWithTracks) {
        eventsWithTracks.maxOfOrNull { it.track } ?: 0
    }

    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        dates.forEach { date ->
            item {
                DayColumn(
                    date = date,
                    eventsWithTracks = eventsWithTracks,
                    tracks = maxTracks + 1,
                    viewMode = viewMode,
                    onEventClick = onEventClick
                )
            }
        }
    }
}

@Composable
fun DayColumn(
    date: Date,
    eventsWithTracks: List<EventTrack>,
    tracks: Int,
    viewMode: CalendarViewMode,
    onEventClick: (Event) -> Unit
) {
    val dayStart = date
    val dayEnd = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, 1)
    }.time

    Column(
        modifier = Modifier
            .width(180.dp)
            .padding(4.dp)
    ) {
        Text(
            text = buildColumnHeader(date, viewMode),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(tracks) { trackIndex ->
                val event = eventsWithTracks.firstOrNull {
                    it.track == trackIndex &&
                            it.start < dayEnd &&
                            it.end >= dayStart
                }

                if (event != null) {
                    EventBlock(event.event) {
                        onEventClick(event.event)
                    }
                } else {
                    Spacer(Modifier.height(56.dp))
                }
            }
        }
    }
}

@Composable
fun EventBlock(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.design_default_color_primary))
    ) {
        Text(
            text = event.name,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun buildColumnHeader(date: Date, viewMode: CalendarViewMode): String {
    val locale = Locale.getDefault()
    return when (viewMode) {
        CalendarViewMode.YEAR ->
            SimpleDateFormat("MMMM", locale).format(date)
        else ->
            SimpleDateFormat("EEE, d", locale).format(date)
    }
}
