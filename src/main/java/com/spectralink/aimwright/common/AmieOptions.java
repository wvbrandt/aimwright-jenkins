package com.spectralink.aimwright.common;

import java.util.Arrays;
import java.util.List;

/**
 * Common options and enums used across the Aimwright test framework.
 */
public class AmieOptions {

    /**
     * Sort order enum for table sorting operations.
     */
    public enum Sort {
        ASC,
        DESC,
        NONE
    }

    /**
     * Account status icon states with their associated CSS colors.
     */
    public enum AccountStatusIconsStates {
        GOOD("color: rgb(0, 128, 0)"),
        WARNING("color: rgb(202, 12, 28)"),
        ERROR("color: rgb(202, 12, 28)");

        private final String color;

        AccountStatusIconsStates(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    // Menu filter options
    public static String menuFilterPlaceholder = "--Please choose--";
    public static String menuFilterTimeRangeDefault = "Last 30 days";
    public static List<String> menuFilterTimeRangeOptions = Arrays.asList(
            "Today",
            "Yesterday",
            "Last 7 days",
            "Last 30 days",
            "Last 60 days",
            "Last 90 days"
    );
}
