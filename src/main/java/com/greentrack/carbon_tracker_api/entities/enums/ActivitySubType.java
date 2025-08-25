package com.greentrack.carbon_tracker_api.entities.enums;

public enum ActivitySubType {

    // Travel subtypes
    CAR_PETROL,
    CAR_DIESEL,
    CAR_ELECTRIC,
    MOTORCYCLE,
    BUS,
    TRAIN,
    FLIGHT_DOMESTIC,
    FLIGHT_INTERNATIONAL,
    BICYCLE, // Zero emissions
    WALKING, // Zero emissions

    // Energy subtypes
    ELECTRICITY_GRID,
    ELECTRICITY_SOLAR,
    NATURAL_GAS,
    HEATING_OIL,

    // Purchase subtypes
    FOOD_MEAT,
    FOOD_VEGETARIAN,
    FOOD_VEGAN,
    ELECTRONICS,
    CLOTHING,
    HOUSEHOLD_ITEMS,
    OTHER_PURCHASES

}
