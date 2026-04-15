package com.kblack.offlinemap.domain.utils

import com.kblack.offlinemap.domain.models.GeoCoordinate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GeoMathTest {

    //todo Example: https://www.vcalc.com/wiki/vcalc/haversine-distance?var-lat1=0&var-lon1=0&var-lat2=0&var-lon2=0
    @Test
    fun `distanceMeters should 0 if the two points are the same`() {
        val geo = GeoCoordinate(latitude = 0.0, longitude = 0.0)

        val actual = GeoMath.distanceMeters(geo, geo)

        assertEquals(0.0, actual, 0.1)
    }

    //todo Example: https://www.vcalc.com/wiki/vcalc/haversine-distance?var-lat1=21.051632&var-lon1=105.812875&var-lat2=21.05063&var-lon2=105.813307
    @Test
    fun `distanceMeters should 120m`() {
        val geoA = GeoCoordinate(latitude = 21.051632, longitude = 105.812875)
        val geoB = GeoCoordinate(latitude = 21.05063, longitude = 105.813307)

        val actual = GeoMath.distanceMeters(geoA, geoB)

        assertEquals(120.0, actual, 0.1)
    }

    @Test
    fun `nearestPointIndex should -1 when path is empty`() {
        val currentLocation = GeoCoordinate(latitude = 21.068633, longitude = 105.803836)
        val nearestPointIdx = GeoMath.nearestPointIndex(emptyList(), currentLocation)

        assertEquals(-1, nearestPointIdx)
    }

    @Test
    fun `nearestPointIndex should 0 when there is only 1 point`() {
        val listGeo = listOf(
            GeoCoordinate(latitude = 21.068013, longitude = 105.804222)
        )
        val currentLocation = GeoCoordinate(latitude = 21.068203, longitude = 105.804251)
        val nearestPointIdx = GeoMath.nearestPointIndex(listGeo, currentLocation)

        assertEquals(0, nearestPointIdx)
    }

    //todo: Example: https://graphhopper.com/maps/?point=21.068013%2C105.804222&point=21.068203%2C105.804251&point=21.069191%2C105.805154&profile=car&layer=Omniscale
    @Test
    fun `nearestPointIndex should 0 when current location near first location`() {
        val listGeo = listOf(
            GeoCoordinate(latitude = 21.068013, longitude = 105.804222),
            GeoCoordinate(latitude = 21.069191, longitude = 105.805154)
        )
        val currentLocation = GeoCoordinate(latitude = 21.068203, longitude = 105.804251)
        val nearestPointIdx = GeoMath.nearestPointIndex(listGeo, currentLocation)

        assertEquals(0, nearestPointIdx)
    }

    //todo: Example: https://graphhopper.com/maps/?point=21.068013%2C105.804222&point=21.069029%2C105.804887&point=21.069191%2C105.805154&profile=car&layer=Omniscale
    @Test
    fun `nearestPointIndex should 1 when current location near last location`() {
        val listGeo = listOf(
            GeoCoordinate(latitude = 21.068013, longitude = 105.804222),
            GeoCoordinate(latitude = 21.069191, longitude = 105.805154)
        )
        val currentLocation = GeoCoordinate(latitude = 21.069029, longitude = 105.804887)
        val nearestPointIdx = GeoMath.nearestPointIndex(listGeo, currentLocation)

        assertEquals(1, nearestPointIdx)
    }

    //todo: Example: https://graphhopper.com/maps/?point=21.068013%2C105.804222&point=21.068633%2C105.803836&point=21.068625%2C105.803478&point=21.068954%2C105.803749&point=21.069316%2C105.803304&point=21.0691%2C105.803096&point=21.068615%2C105.804188&point=21.069191%2C105.805154&profile=car&layer=Omniscale
    @Test
    fun `nearestPointIndex should 5 when current location near fifth location`() {

        val listGeo = listOf(
            GeoCoordinate(latitude = 21.068013, longitude = 105.804222),
            GeoCoordinate(latitude = 21.068625, longitude = 105.803478),
            GeoCoordinate(latitude = 21.068954, longitude = 105.803749),
            GeoCoordinate(latitude = 21.069316, longitude = 105.803304),
            GeoCoordinate(latitude = 21.0691, longitude = 105.803096),
            GeoCoordinate(latitude = 21.068615, longitude = 105.804188),
            GeoCoordinate(latitude = 21.069191, longitude = 105.805154)
        )
        val currentLocation = GeoCoordinate(latitude = 21.068633, longitude = 105.803836)
        val nearestPointIdx = GeoMath.nearestPointIndex(listGeo, currentLocation)

        assertEquals(5, nearestPointIdx)
    }

    // todo: Example: https://graphhopper.com/maps/?point=21.051475%2C105.812944&point=21.051378%2C105.812839&point=21.05176%2C105.812831&profile=car&layer=Omniscale
    @Test
    fun `distancePointToSegmentMeters should distance to A when projection falls before segment`() {
        val a = GeoCoordinate(latitude = 21.051475, longitude = 105.812944)
        val b = GeoCoordinate(latitude = 21.05176, longitude = 105.812831)
        val p = GeoCoordinate(latitude = 21.051378, longitude = 105.812839)

        val expected = GeoMath.distanceMeters(p, a)
        val distance = GeoMath.distancePointToSegmentMeters(p, a, b)

        assertEquals(expected, distance, 0.1)
    }

    // todo: Example: https://graphhopper.com/maps/?point=21.051475%2C105.812944&point=21.051779%2C105.812716&point=21.05176%2C105.812831&profile=car&layer=Omniscale
    @Test
    fun `distancePointToSegmentMeters should distance to B when projection falls after segment`() {
        val a = GeoCoordinate(latitude = 21.051475, longitude = 105.812944)
        val b = GeoCoordinate(latitude = 21.05176, longitude = 105.812831)
        val p = GeoCoordinate(latitude = 21.051779, longitude = 105.812716)

        val expected = GeoMath.distanceMeters(p, b)
        val distance = GeoMath.distancePointToSegmentMeters(p, a, b)

        assertEquals(expected, distance, 0.1)
    }

    // todo: Example: https://graphhopper.com/maps/?point=21.051475%2C105.812944&point=21.051631%2C105.812882&point=21.05176%2C105.812831&profile=car&layer=Omniscale
    @Test
    fun `distancePointToSegmentMeters should near-zero when point is on the segment`() {
        val a = GeoCoordinate(latitude = 21.051475, longitude = 105.812944)
        val b = GeoCoordinate(latitude = 21.05176, longitude = 105.812831)
        val p = GeoCoordinate(latitude = 21.051631, longitude = 105.812882)

        val distance = GeoMath.distancePointToSegmentMeters(p, a, b)
        //if delta = 0 => expected = 0.014358607825704724
        assertEquals(0.0, distance, 0.1)
    }

    // todo: Example: https://graphhopper.com/maps/?point=21.051475%2C105.812831&point=21.05176%2C105.812831&profile=car&layer=Omniscale
    @Test
    fun `bearingDegrees should 0 degrees when moving due north`() {
        val long = 105.812831
        val from = GeoCoordinate(latitude = 21.051475, longitude = long)
        val to = GeoCoordinate(latitude = 21.05176, longitude = long)

        val actual = GeoMath.bearingDegrees(from, to)

        assertEquals(0.0, actual, 0.1)
    }

    @Test
    fun `bearingDegrees should 180 degrees when moving due south`() {
        val long = 105.812831
        val from = GeoCoordinate(latitude = 21.05176, longitude = long)
        val to = GeoCoordinate(latitude = 21.051475, longitude = long)

        val actual = GeoMath.bearingDegrees(from, to)

        assertEquals(180.0, actual, 0.1)
    }

    // todo: Example: https://graphhopper.com/maps/?point=21.051475%2C105.812831&point=21.051475%2C105.812944&profile=car&layer=Omniscale
    @Test
    fun `bearingDegrees should 90 degrees when moving due east`() {
        val lat = 21.051475
        val from = GeoCoordinate(latitude = lat, longitude = 105.812831)
        val to = GeoCoordinate(latitude = lat, longitude = 105.812944)

        val actual = GeoMath.bearingDegrees(from, to)

        assertEquals(90.0, actual, 0.1)
    }

}

// longitude tăng = đông, longitude giảm = tây; latitude tăng = bắc, latitude giảm = nam
// North (0°), South (180°), East (90°), West (270°)
