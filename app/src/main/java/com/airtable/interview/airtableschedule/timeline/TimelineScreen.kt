package com.airtable.interview.airtableschedule.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.Event
import java.text.SimpleDateFormat
import java.util.*

/* ---------------- VIEW MODE ---------------- */

enum class CalendarViewMode {
    WEEK, MONTH, YEAR
}

/* ---------------- SCREEN ---------------- */

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf(CalendarViewMode.WEEK) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    val baseDate = remember { Date() }

    val pagerState = rememberPagerState(
        initialPage = 500,
        pageCount = { 1000 }
    )

    Column(modifier = Modifier.fillMaxSize()) {

        CalendarHeader(
            selectedMode = viewMode,
            onModeChange = { viewMode = it }
        )

        Text(
            text = buildPeriodHeader(
                page = pagerState.currentPage,
                viewMode = viewMode,
                baseDate = baseDate
            ),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(12.dp)
        )

        HorizontalDivider()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val dates = getDatesForPage(page, viewMode, baseDate)

            TimelineGrid(
                dates = dates,
                events = uiState.events,
                onEventClick = { selectedEvent = it }
            )
        }
    }

    selectedEvent?.let {
        EventDetailsDialog(it) { selectedEvent = null }
    }
}

/* ---------------- HEADER ---------------- */

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
        CalendarViewMode.values().forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeChange(mode) },
                label = { Text(mode.name) }
            )
        }
    }
}

/* ---------------- PERIOD TITLE ---------------- */

fun buildPeriodHeader(
    page: Int,
    viewMode: CalendarViewMode,
    baseDate: Date
): String {
    val cal = Calendar.getInstance()
    cal.time = baseDate

    when (viewMode) {
        CalendarViewMode.WEEK -> {
            cal.add(Calendar.WEEK_OF_YEAR, page - 500)
            return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        }

        CalendarViewMode.MONTH -> {
            cal.add(Calendar.MONTH, page - 500)
            return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        }

        CalendarViewMode.YEAR -> {
            cal.add(Calendar.YEAR, page - 500)
            return cal.get(Calendar.YEAR).toString()
        }
    }
}

/* ---------------- GRID ---------------- */

@Composable
fun TimelineGrid(
    dates: List<Date>,
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val normalizedEvents = remember(events) {
        events.sortedBy { fixLegacyDate(it.startDate) }
    }

    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(dates) { date ->
            DayColumn(
                date = fixLegacyDate(date),
                events = normalizedEvents,
                onEventClick = onEventClick
            )
        }
    }
}

/* ---------------- DAY COLUMN ---------------- */

@Composable
fun DayColumn(
    date: Date,
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val dayStart = date
    val dayEnd = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, 1)
    }.time

    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp)
    ) {
        Text(
            text = SimpleDateFormat("d", Locale.getDefault()).format(date),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(8.dp))

        events.forEach { event ->
            val start = fixLegacyDate(event.startDate)
            val end = fixLegacyDate(event.endDate)

            val intersects = start < dayEnd && end >= dayStart

            if (intersects) {
                EventBlock(event) { onEventClick(event) }
            } else {
                Spacer(Modifier.height(56.dp))
            }
        }
    }
}

/* ---------------- EVENT ---------------- */

@Composable
fun EventBlock(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = event.name,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/* ---------------- DATES ---------------- */

fun getDatesForPage(
    page: Int,
    viewMode: CalendarViewMode,
    baseDate: Date
): List<Date> {
    val cal = Calendar.getInstance()
    cal.time = baseDate

    when (viewMode) {
        CalendarViewMode.WEEK -> {
            cal.add(Calendar.WEEK_OF_YEAR, page - 500)
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            return List(7) {
                val date = cal.time
                cal.add(Calendar.DAY_OF_MONTH, 1)
                date
            }
        }

        CalendarViewMode.MONTH -> {
            cal.add(Calendar.MONTH, page - 500)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            return List(days) {
                val date = cal.time
                cal.add(Calendar.DAY_OF_MONTH, 1)
                date
            }
        }

        CalendarViewMode.YEAR -> {
            cal.add(Calendar.YEAR, page - 500)
            cal.set(Calendar.MONTH, 0)
            return List(12) {
                val date = cal.time
                cal.add(Calendar.MONTH, 1)
                date
            }
        }
    }
}

/* ---------------- EVENT DIALOG ---------------- */

@Composable
fun EventDetailsDialog(
    event: Event,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text(event.name) },
        text = {
            Text(
                "Start: ${event.startDate}\nEnd: ${event.endDate}"
            )
        }
    )
}

/* ---------------- DATE FIX ---------------- */
/* Corrige Date(year, month, day) legado */

fun fixLegacyDate(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}
