package com.kblack.offlinemap.data.mapper

import com.kblack.offlinemap.data.local.entity.PlaceSearchEntity
import com.kblack.offlinemap.data.models.Feature
import com.kblack.offlinemap.models.PlaceSearch

fun Feature.toPlaceSearch(): PlaceSearch = PlaceSearch(
    osmId = properties.osmId,
    osmValue = properties.osmValue,
    name = properties.name,
    street = properties.street,
    locality = properties.locality,
    district = properties.district,
    city = properties.city,
    state = properties.state,
    country = properties.country,
    postcode = properties.postcode,
    lat = geometry.lat,
    lng = geometry.lng,
)

fun PlaceSearch.toPlaceSearchEntity(): PlaceSearchEntity = PlaceSearchEntity(
    osmId = osmId,
    osmValue = osmValue,
    name = name,
    street = street,
    locality = locality,
    district = district,
    city = city,
    state = state,
    country = country,
    postcode = postcode,
    lat = lat,
    lng = lng
)

fun PlaceSearchEntity.toPlaceSearch(): PlaceSearch = PlaceSearch(
    osmId = osmId,
    osmValue = osmValue,
    name = name,
    street = street,
    locality = locality,
    district = district,
    city = city,
    state = state,
    country = country,
    postcode = postcode,
    lat = lat,
    lng = lng
)