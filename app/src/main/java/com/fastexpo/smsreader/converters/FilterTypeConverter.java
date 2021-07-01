package com.fastexpo.smsreader.converters;

import androidx.room.TypeConverter;

import com.fastexpo.smsreader.enums.FilterType;

import java.util.Date;
import java.util.logging.Filter;

public class FilterTypeConverter {

    @TypeConverter
    public static String fromFilterType(FilterType value) {
        return value.name();
    }

    @TypeConverter
    public static FilterType toFilterType(String value) {
        return FilterType.valueOf(value);
    }
}