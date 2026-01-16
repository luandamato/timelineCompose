package com.airtable.interview.airtableschedule.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.Event
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/* =========================================================
 * VIEW MODE
 * ========================================================= */

enum class CalendarViewMode {
    WEEK, MONTH, YEAR
}

/* =========================================================
 * SCREEN
 * ========================================================= */

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var viewMode by remember { mutableStateOf(CalendarViewMode.WEEK) }
    val baseDate = remember { todayAtMidnight() }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    val pagerState = rememberPagerState(
        initialPage = 500,
        pageCount = { 1000 }
    )

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        CalendarHeader(
            selectedMode = viewMode,
            onModeChange = { viewMode = it }
        )

        PeriodNavigationHeader(
            title = buildPeriodHeader(
                baseDate = baseDate,
                page = pagerState.currentPage,
                viewMode = viewMode
            ),
            onPrevious = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            onNext = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        )

        HorizontalDivider()

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val dates = getDatesForPage(
                page = page,
                baseDate = baseDate,
                viewMode = viewMode
            )

            val eventsWithTracks = remember(uiState.events) {
                assignTracks(uiState.events)
            }

            TimelineGrid(
                dates = dates,
                eventsWithTracks = eventsWithTracks,
                viewMode = viewMode,
                onEventClick = { selectedEvent = it }
            )
        }
    }

    selectedEvent?.let {
        EventDetailsDialog(it) { selectedEvent = null }
    }
}

/* =========================================================
 * HEADER
 * ========================================================= */

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

/* =========================================================
 * PERIOD NAVIGATION
 * ========================================================= */

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

/* =========================================================
 * GRID
 * ========================================================= */

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

/* =========================================================
 * DAY COLUMN (VERTICAL SCROLL FIXED HERE)
 * ========================================================= */

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

/* =========================================================
 * EVENT BLOCK
 * ========================================================= */

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
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = event.name,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/* =========================================================
 * DIALOG
 * ========================================================= */

@Composable
fun EventDetailsDialog(
    event: Event,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        },
        title = { Text(event.name) },
        text = {
            Column {
                Text("Início: ${event.startDate}")
                Text("Fim: ${event.endDate}")
            }
        }
    )
}

/* =========================================================
 * TEXT BUILDERS
 * ========================================================= */

fun buildPeriodHeader(
    baseDate: Date,
    page: Int,
    viewMode: CalendarViewMode
): String {
    val cal = Calendar.getInstance()
    cal.time = baseDate

    when (viewMode) {
        CalendarViewMode.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, page - 500)
        CalendarViewMode.MONTH -> cal.add(Calendar.MONTH, page - 500)
        CalendarViewMode.YEAR -> cal.add(Calendar.YEAR, page - 500)
    }

    val locale = Locale.getDefault()

    return when (viewMode) {
        CalendarViewMode.YEAR ->
            SimpleDateFormat("yyyy", locale).format(cal.time)
        else ->
            SimpleDateFormat("MMMM yyyy", locale).format(cal.time)
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

/* =========================================================
 * DATE CALCULATION
 * ========================================================= */

fun getDatesForPage(
    page: Int,
    baseDate: Date,
    viewMode: CalendarViewMode
): List<Date> {
    val cal = Calendar.getInstance()
    cal.time = baseDate

    when (viewMode) {
        CalendarViewMode.WEEK -> {
            cal.add(Calendar.WEEK_OF_YEAR, page - 500)
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            return List(7) {
                val d = cal.time
                cal.add(Calendar.DAY_OF_MONTH, 1)
                d
            }
        }

        CalendarViewMode.MONTH -> {
            cal.add(Calendar.MONTH, page - 500)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            return List(days) {
                val d = cal.time
                cal.add(Calendar.DAY_OF_MONTH, 1)
                d
            }
        }

        CalendarViewMode.YEAR -> {
            cal.add(Calendar.YEAR, page - 500)
            cal.set(Calendar.MONTH, 0)
            return List(12) {
                val d = cal.time
                cal.add(Calendar.MONTH, 1)
                d
            }
        }
    }
}

/* =========================================================
 * TRACK ENGINE
 * ========================================================= */

data class EventTrack(
    val event: Event,
    val start: Date,
    val end: Date,
    val track: Int
)

fun assignTracks(events: List<Event>): List<EventTrack> {
    val sorted = events.sortedBy { it.startDate }
    val trackEndDates = mutableListOf<Date>()

    return sorted.map { event ->
        val index = trackEndDates.indexOfFirst { it.before(event.startDate) }

        val track = if (index >= 0) {
            trackEndDates[index] = event.endDate
            index
        } else {
            trackEndDates.add(event.endDate)
            trackEndDates.lastIndex
        }

        EventTrack(
            event = event,
            start = normalize(event.startDate),
            end = normalize(event.endDate),
            track = track
        )
    }
}

/* =========================================================
 * UTILS
 * ========================================================= */

fun normalize(date: Date): Date =
    Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

fun todayAtMidnight(): Date = normalize(Date())
