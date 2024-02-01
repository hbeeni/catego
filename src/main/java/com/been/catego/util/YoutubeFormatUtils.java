package com.been.catego.util;

import com.google.api.client.util.DateTime;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class YoutubeFormatUtils {

    private YoutubeFormatUtils() {
    }

    public static String formatSubscriberCount(BigInteger subscriberCount) {
        return formatCount(subscriberCount, "#.##");
    }

    public static String formatViewCount(BigInteger viewCount) {
        return formatCount(viewCount, "#.#");
    }

    public static String formatDateTime(DateTime dateTime) {
        LocalDateTime koreaLDT = YoutubeConvertUtils.convertToKoreaLocalDateTime(dateTime);
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(koreaLDT, now);
        long hours = ChronoUnit.HOURS.between(koreaLDT, now);
        long days = ChronoUnit.DAYS.between(koreaLDT, now);
        long months = ChronoUnit.MONTHS.between(koreaLDT, now);
        long years = ChronoUnit.YEARS.between(koreaLDT, now);

        if (years > 0) {
            return years + "년 전";
        } else if (months > 0) {
            return months + "개월 전";
        } else if (days > 0) {
            return days + "일 전";
        } else if (hours > 0) {
            return hours + "시간 전";
        } else if (minutes > 0) {
            return minutes + "분 전";
        } else {
            return "방금 전";
        }
    }

    public static String formatVideoCount(BigInteger videoCount) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(videoCount);
    }

    private static String formatCount(BigInteger count, String format) {
        double n = count.doubleValue();
        DecimalFormat decimalFormat = new DecimalFormat(format);

        if (n > 100000000) {
            return decimalFormat.format(n / 100000000) + "억";
        } else if (n > 10000) {
            return decimalFormat.format(n / 10000) + "만";
        } else if (n > 1000) {
            return decimalFormat.format(n / 1000) + "천";
        } else if (n > 100) {
            return decimalFormat.format(n / 100) + "백";
        } else {
            return String.valueOf(n);
        }
    }
}
