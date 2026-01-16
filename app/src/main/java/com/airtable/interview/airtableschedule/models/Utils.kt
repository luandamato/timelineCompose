package com.airtable.interview.airtableschedule.models

import java.util.Calendar
import java.util.Date

/**
 * Takes a list of [Event]s and assigns them to lanes based on start/end dates.
 */

fun normalize(date: Date): Date =
    Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

fun todayAtMidnight(): Date = normalize(Date())
