package com.example.project

import net.daum.mf.map.api.MapPOIItem

class MarkerFilter {
    fun filterMarkers(markers: List<MapPOIItem>, centerLatitude: Double, centerLongitude: Double, radiusInKm: Double): List<MapPOIItem> {
        val filteredMarkers = mutableListOf<MapPOIItem>()
        for (marker in markers) {
            val markerLatitude = marker.mapPoint.mapPointGeoCoord.latitude
            val markerLongitude = marker.mapPoint.mapPointGeoCoord.longitude
            val distance = distanceInKmBetweenEarthCoordinates(centerLatitude, centerLongitude, markerLatitude, markerLongitude)
            if (distance <= radiusInKm) {
                filteredMarkers.add(marker)
            }
        }
        return filteredMarkers
    }

    private fun degreesToRadians(degrees: Double): Double {
        return degrees * Math.PI / 180
    }

    fun distanceInKmBetweenEarthCoordinates(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371

        val dLat = degreesToRadians(lat2 - lat1)
        val dLon = degreesToRadians(lon2 - lon1)

        val lat1Rad = degreesToRadians(lat1)
        val lat2Rad = degreesToRadians(lat2)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadiusKm * c
    }
}
