package com.been.catego.util;

import com.google.api.client.util.DateTime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class ConvertUtil {

    private ConvertUtil() {
    }

    public static LocalDateTime convertToLocalDateTime(DateTime dateTime) {
        Instant instant = Instant.ofEpochMilli(dateTime.getValue());
        return LocalDateTime.ofInstant(instant, ZoneId.of("GMT"));
    }

    public static LocalDateTime convertToKoreaLocalDateTime(DateTime dateTime) {
        Instant instant = Instant.ofEpochMilli(dateTime.getValue());
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }
}
