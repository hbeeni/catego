package com.been.catego.util;

import java.math.BigInteger;
import java.text.DecimalFormat;

public abstract class FormatUtil {

    private FormatUtil() {
    }

    public static String formatSubscriberCount(BigInteger subscriberCount) {
        double n = subscriberCount.doubleValue();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        if (n > 10000) {
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
