package com.assessmenttest.constants


object Consts {
    const val PERMISSIONS_REQUEST_LOCATION = 99

    internal object GoogleApiTags {
        const val BASE_URL = "https://maps.googleapis.com/"
        const val PLACES_DIRECTIONS_EXT_URL = "maps/api/directions/json"
    }

    object DateFormat{
        const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val CALENDAR_FORMAT = "MM/dd/yyyy"
       const val FETCH_DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss"
    }

    object ApiStatusCode {
        const val REQUEST_RESULT_OK = "OK"
    }

    internal object GoogleApiParams {
        const val ORIGIN = "origin"
        const val DESTINATION = "destination"
        const val KEY = "key"
    }

    object Character {
        const val SPACE = " "
        const val COMMA = ","
        const val UNDER_SCORE = "_"
        const val SLASH = "/"
        const val OPEN_BRACKET = "("
    }
}