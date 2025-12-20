package com.ensao.mytime.Activityfeature;

import androidx.room.TypeConverter;

import java.util.Date;



//we need to use converter because room does not allow reference types like Date or costom classes
public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }


}
