package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

object RemindersMockList {
    val list = arrayListOf(
        ReminderDTO(
            title = "TITLE1",
            description = "DESCRIPTION1",
            latitude = -18.92149970366,
            location = "LOCATION1",
            longitude = -48.209044831600,
            id = "ID1"
        ),
        ReminderDTO(
            title = "TITLE2",
            description = "DESCRIPTION2",
            latitude = -18.92149970366,
            location = "LOCATION2",
            longitude = -48.209044831600,
            id = "ID2"
        ),
        ReminderDTO(
            title = "TITLE3",
            description = "DESCRIPTION3",
            latitude = -18.92149970366,
            location = "LOCATION3",
            longitude = -48.209044831600,
            id = "ID3"
        )
    )

    val reminderItem =
        ReminderDataItem(
            "REMINDER ITEM TITLE",
            "REMINDER ITEM DESCRIPTION",
            "REMINDER ITEM LOCATION",
            -18.92149970366,
            -48.209044831600
        )
}