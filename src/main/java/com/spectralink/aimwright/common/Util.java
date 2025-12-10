package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * General utility methods for the Aimwright framework.
 */
public class Util {
    private static final Logger log = (Logger) LoggerFactory.getLogger(Util.class.getName());

    /**
     * Rounds a decimal value to a percentage with specified precision.
     *
     * @param rawValue The decimal value (0.5 = 50%)
     * @param precisionLimit Number of decimal places
     * @return Rounded percentage value
     */
    public static double getRoundedPercentage(double rawValue, int precisionLimit) {
        double divisor = Math.pow(10, precisionLimit);
        double rawPercentage = rawValue * 100;
        double roundedValue = Math.round(rawPercentage * divisor);
        return roundedValue / divisor;
    }

    /**
     * Rounds a number to specified decimal places.
     *
     * @param rawValue The value to round
     * @param precisionLimit Number of decimal places
     * @return Rounded value
     */
    public static double getRoundedNumber(double rawValue, int precisionLimit) {
        double divisor = Math.pow(10, precisionLimit);
        double roundedValue = Math.round(rawValue * divisor);
        return roundedValue / divisor;
    }
}
