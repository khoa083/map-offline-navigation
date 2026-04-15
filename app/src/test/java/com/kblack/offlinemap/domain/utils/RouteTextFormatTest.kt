package com.kblack.offlinemap.domain.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RouteTextFormatTest {

    @Test
    fun `formatDistanceMeters should return 0 meter when distance is 0`() {
        val distanceMeters = 0.0
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters)
        assertEquals("0 meter", actual)
    }

    @Test
    fun `formatDistanceMeters should return meters when distance less than 1000m`() {
        val distanceMeters = 500.0
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters)
        assertEquals("500 meter", actual)
    }

    @Test
    fun `formatDistanceMeters should return 1 km when distance is 1000m`() {
        val distanceMeters = 1000.0
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters)
        assertEquals("1.0 km", actual)
    }

    @Test
    fun `formatDistanceMeters should return km with 1 decimal when distance greater than 1km`() {
        val distanceMeters = 1234.5
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters)
        assertEquals("1.2 km", actual)
    }

    @Test
    fun `formatDistanceMeters should return negative values as 0`() {
        val distanceMeters = -500.0
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters)
        assertEquals("0 meter", actual)
    }


    @Test
    fun `formatDistanceMeters should return feet when useImperial true and distance less than 1 mile`() {
        val distanceMeters = 500.0
        val useImperial = true
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters, useImperial)
        assertEquals("1640 feet", actual)
    }

    @Test
    fun `formatDistanceMeters should return miles when useImperial true and distance greater than 1 mile`() {
        val distanceMeters = 1609.344
        val useImperial = true
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters, useImperial)
        assertEquals("1.0 mi", actual)
    }

    @Test
    fun `formatDistanceMeters should return miles with 1 decimal when useImperial true`() {
        val distanceMeters = 2000.0
        val useImperial = true
        val actual = RouteTextFormatter.formatDistanceMeters(distanceMeters, useImperial)
        assertEquals("1.2 mi", actual)
    }


    @Test
    fun `formatDurationMillis should return 0 min when duration is 0`() {
        val durationMillis = 0L
        val actual = RouteTextFormatter.formatDurationMillis(durationMillis)
        assertEquals("0 min", actual)
    }

    @Test
    fun `formatDurationMillis should return minutes when duration less than 1 hour`() {
        val durationMillis = 300000L
        val actual = RouteTextFormatter.formatDurationMillis(durationMillis)
        assertEquals("5 min", actual)
    }

    @Test
    fun `formatDurationMillis should return 1 min when duration is 1 minute`() {
        val durationMillis = 60000L
        val actual = RouteTextFormatter.formatDurationMillis(durationMillis)
        assertEquals("1 min", actual)
    }

    @Test
    fun `formatDurationMillis should return hours and minutes when duration greater than 1 hour`() {
        val durationMillis = 5400000L
        val actual = RouteTextFormatter.formatDurationMillis(durationMillis)
        assertEquals("1 h: 30 m", actual)
    }

    @Test
    fun `formatDurationMillis should return hours and minutes when duration is exactly 2 hours`() {
        val durationMillis = 7200000L
        val actual = RouteTextFormatter.formatDurationMillis(durationMillis)
        assertEquals("2 h: 0 m", actual)
    }

    @Test
    fun `formatDurationMillis should return negative values as 0 min`() {
        val durationMillis = -300000L
        val actual = RouteTextFormatter.formatDurationMillis(durationMillis)
        assertEquals("0 min", actual)
    }

    @Test
    fun `formatDurationMillis should round to nearest minute`() {
        val durationMillis = 330000L
        val actual = RouteTextFormatter.formatDurationMillis(durationMillis)
        assertEquals("6 min", actual)
    }

}