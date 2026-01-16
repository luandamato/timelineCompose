package com.airtable.interview.airtableschedule.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.Event
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.airtable.interview.airtableschedule.models.normalize
import com.airtable.interview.airtableschedule.models.todayAtMidnight
import com.airtable.interview.airtableschedule.timeline.ui.CalendarHeader
import com.airtable.interview.airtableschedule.timeline.ui.EventDetailsDialog
import com.airtable.interview.airtableschedule.timeline.ui.PeriodNavigationHeader
import com.airtable.interview.airtableschedule.timeline.ui.TimelineGrid

/* =========================================================
 * VIEW MODE
 * ========================================================= */

enum class CalendarViewMode {
    WEEK, MONTH, YEAR
}

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

