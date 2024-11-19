package com.p2p.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

public final class MoneyUtils {
    private MoneyUtils() {}

    public static final Currency DEFAULT_CURRENCY = Currency.getInstance("HKD");
    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static BigDecimal toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, DEFAULT_ROUNDING);
    }

    public static BigDecimal fromMinorUnits(BigDecimal minorUnits) {
        return minorUnits.divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static String format(BigDecimal amount) {
        return String.format(
            Locale.US,
            "%s %,.2f",
            DEFAULT_CURRENCY.getSymbol(),
            amount
        );
    }

    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }
} 