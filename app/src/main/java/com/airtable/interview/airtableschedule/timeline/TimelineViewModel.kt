package com.airtable.interview.airtableschedule.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airtable.interview.airtableschedule.repositories.EventDataRepository
import com.airtable.interview.airtableschedule.repositories.EventDataRepositoryImpl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel responsible for managing the state of the timeline screen.
 */
class TimelineViewModel: ViewModel() {
    private val eventDataRepository: EventDataRepository = EventDataRepositoryImpl()

    val uiState: StateFlow<TimelineUiState> = eventDataRepository
        .getTimelineItems()
        .map(::TimelineUiState)
        .stateIn(
            viewModelScope,
            initialValue = TimelineUiState(),
            started = SharingStarted.WhileSubscribed()
        )
}
