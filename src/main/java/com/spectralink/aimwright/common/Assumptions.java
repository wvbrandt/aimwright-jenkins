package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;

/**
 * Test precondition utilities for the Aimwright framework.
 *
 * Use Assumptions to verify that expected test data exists in AMiE.
 * If preconditions are not met, the test is skipped (not failed).
 *
 * For coding errors, missing UI elements, or product failures,
 * use either TestNG Assert or the Aimwright Asserts class instead.
 */
public class Assumptions {

    private static final Logger log = (Logger) LoggerFactory.getLogger(Assumptions.class.getName());

    /**
     * Skips the test if the condition is true.
     *
     * @param value The condition to check
     * @param message Message to log if skipping
     */
    public static void assumeFalse(boolean value, String message) {
        if (!value) {
            log.trace("Precondition met, continuing test execution");
        } else {
            log.warn(message);
            throw new SkipException(message);
        }
    }

    /**
     * Skips the test if the condition is false.
     * Commonly used to verify sufficient test data exists.
     *
     * @param value The condition to check
     * @param message Message to log if skipping
     */
    public static void assumeTrue(boolean value, String message) {
        if (value) {
            log.trace("Precondition met, continuing test execution");
        } else {
            log.warn(message);
            throw new SkipException(message);
        }
    }

    /**
     * Skips the test if the object is null.
     *
     * @param object The object to check
     * @param message Message to log if skipping
     */
    public static void assumeNotNull(Object object, String message) {
        if (object == null) {
            String formatted = (message != null) ? message : "Object was null";
            log.warn(formatted);
            throw new SkipException(formatted);
        }
    }

    /**
     * Skips the test if the object is not null.
     *
     * @param object The object to check
     * @param message Message to log if skipping
     */
    public static void assumeNull(Object object, String message) {
        if (object != null) {
            String formatted = (message != null) ? message : "Object was not null";
            log.warn(formatted);
            throw new SkipException(formatted);
        }
    }

    /**
     * Skips the test if there are fewer than the minimum required rows.
     * Commonly used to ensure sufficient test data exists for sorting tests.
     *
     * @param actualRows The actual number of rows
     * @param minimumRows The minimum required rows
     * @param message Message to log if skipping
     */
    public static void assumeMinimumRows(int actualRows, int minimumRows, String message) {
        if (actualRows < minimumRows) {
            String formatted = (message != null) ? message :
                String.format("Need at least %d rows, but only found %d", minimumRows, actualRows);
            log.warn(formatted);
            throw new SkipException(formatted);
        }
        log.trace("Minimum row precondition met: {} >= {}", actualRows, minimumRows);
    }
}
