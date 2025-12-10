package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import com.microsoft.playwright.Page;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.testng.Assert.*;

/**
 * Custom assertion methods for the Aimwright test framework.
 * Includes sorting validations and verbose assertions with logging.
 *
 * Adapted for Playwright - removes Selenium-specific dependencies.
 */
public class Asserts {
    private static final Logger log = (Logger) LoggerFactory.getLogger(Asserts.class.getName());

    /**
     * Validates that a list of strings is alphabetically sorted.
     *
     * @param list The list to validate
     * @param sortOrder Expected sort order (ASC or DESC)
     * @param ignoreEmptyValues If true, empty values are moved to end of list
     */
    public static void isAlphabeticallySorted(List<String> list, AmieOptions.Sort sortOrder, boolean ignoreEmptyValues) {
        List<String> lowercase = new ArrayList<>();
        for (String element : list) {
            lowercase.add(element.toLowerCase());
        }
        List<String> sorted = new ArrayList<>(lowercase);
        if (sortOrder == AmieOptions.Sort.ASC) {
            Collections.sort(sorted);
        } else if (sortOrder == AmieOptions.Sort.DESC) {
            Collections.sort(sorted, Collections.reverseOrder());
        }
        if (ignoreEmptyValues) {
            Iterator<String> iterator = sorted.iterator();
            int emptyValueCount = 0;
            while (iterator.hasNext()) {
                String s = iterator.next();
                if (s.isEmpty()) {
                    iterator.remove();
                    emptyValueCount++;
                }
            }
            log.debug("Empty entries moved to the end: {}", emptyValueCount);
            for (int k = 1; k <= emptyValueCount; k++) {
                sorted.add("");
            }
        }
        log.debug("On page data for {} sort: {}", sortOrder, lowercase);
        log.debug("Expected data for {} sort: {}", sortOrder, sorted);
        assertNotEquals(sorted, Collections.emptyList(), "Empty list");
        assertEquals(sorted, lowercase, "String list not sorted");
    }

    /**
     * Validates that a list of numeric strings is numerically sorted.
     *
     * @param list The list to validate (strings that parse to doubles)
     * @param sortOrder Expected sort order (ASC or DESC)
     */
    public static void isNumericallySorted(List<String> list, AmieOptions.Sort sortOrder) {
        List<Double> numbers = new ArrayList<>();
        for (String element : list) {
            numbers.add(Double.parseDouble(element));
        }
        List<Double> sorted = new ArrayList<>(numbers);
        if (sortOrder == AmieOptions.Sort.ASC) {
            Collections.sort(sorted);
        } else if (sortOrder == AmieOptions.Sort.DESC) {
            Collections.sort(sorted, Collections.reverseOrder());
        }
        log.debug("On page data for {} sort: {}", sortOrder, numbers);
        log.debug("Expected data for {} sort: {}", sortOrder, sorted);
        assertNotEquals(sorted, Collections.emptyList(), "Empty list");
        assertEquals(sorted, numbers, "Number list not sorted");
    }

    /**
     * Validates that a list of date strings is sorted by time using default format.
     *
     * @param list The list to validate
     * @param sortOrder Expected sort order (ASC or DESC)
     */
    public static void isTimeSorted(List<String> list, AmieOptions.Sort sortOrder) {
        isTimeSorted(list, sortOrder, "MMM dd yyyy HH:mm", true);
    }

    /**
     * Validates that a list of date strings is sorted by time.
     *
     * @param list The list to validate
     * @param sortOrder Expected sort order (ASC or DESC)
     * @param dateFormat The date format pattern (e.g., "MMM dd yyyy HH:mm")
     * @param ignoreNoDateValues If true, empty/N/A values are treated as epoch 0
     */
    public static void isTimeSorted(List<String> list, AmieOptions.Sort sortOrder, String dateFormat, boolean ignoreNoDateValues) {
        List<Date> dates = new ArrayList<>();
        for (String element : list) {
            if (!element.isEmpty() && !element.equals("N/A")) {
                try {
                    dates.add(new SimpleDateFormat(dateFormat).parse(element));
                } catch (Exception e) {
                    log.error("Failed to parse date '{}' with format '{}': {}", element, dateFormat, e.getMessage());
                }
            } else if ((element.isEmpty() || element.equals("N/A")) && ignoreNoDateValues) {
                dates.add(new Date(0));
            } else {
                assertTrue(ignoreNoDateValues, "Unexpected date format and ignoreNoDateValues is false");
                log.debug("Date format was unexpected");
            }
        }
        List<Date> sorted = new ArrayList<>(dates);
        if (sortOrder == AmieOptions.Sort.ASC) {
            Collections.sort(sorted);
        } else if (sortOrder == AmieOptions.Sort.DESC) {
            Collections.sort(sorted, Collections.reverseOrder());
        }
        log.debug("On page data for {} sort: {}", sortOrder, dates);
        log.debug("Expected data for {} sort: {}", sortOrder, sorted);
        assertNotEquals(sorted, Collections.emptyList());
        assertEquals(sorted, dates);
    }

    /**
     * Validates the current page URL contains the expected endpoint.
     * Uses Playwright's page.url() instead of Selenium WebDriver.
     *
     * @param page The Playwright page instance
     * @param endpoint The expected endpoint (e.g., "/devices/summary")
     */
    public static void onPage(Page page, String endpoint) {
        String currentUrl = page.url();
        String expectedUrl = Settings.getUiInstance() + endpoint;

        if (endpoint.isEmpty()) {
            expectedUrl = Settings.getUiInstance() + "/";
        }

        if (!currentUrl.contains(endpoint.isEmpty() ? Settings.getUiInstance() : endpoint)) {
            log.error("Expected URL containing '{}', but was '{}'", endpoint, currentUrl);
        }

        assertEquals(currentUrl, expectedUrl, "Page URL mismatch");
    }

    /**
     * Verbose equals assertion that logs before failing.
     */
    public static void verboseEquals(Object firstComparator, Object secondComparator, String failMessage) {
        if (firstComparator.equals(secondComparator)) {
            log.debug(failMessage.replace("was not ", "was "));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Verbose not-equals assertion that logs before failing.
     */
    public static void verboseNotEquals(Object firstComparator, Object secondComparator, String failMessage) {
        if (!firstComparator.equals(secondComparator)) {
            log.debug(failMessage.replace("was ", "was not "));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Verbose contains assertion that logs before failing.
     */
    public static void verboseContains(String firstComparator, String secondComparator, String failMessage) {
        if (firstComparator.contains(secondComparator)) {
            log.debug(failMessage.replace("did not", "did"));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Verbose not-contains assertion that logs before failing.
     */
    public static void verboseNotContains(String firstComparator, String secondComparator, String failMessage) {
        if (!firstComparator.contains(secondComparator)) {
            log.debug(failMessage.replace("did", "did not"));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Verbose greater-or-equals assertion that logs before failing.
     */
    public static void verboseGreaterOrEquals(int firstComparator, int secondComparator, String failMessage) {
        if (firstComparator >= secondComparator) {
            log.debug(failMessage.replace("was not ", "was "));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Verbose array contains assertion that logs before failing.
     */
    public static void verboseArrayContains(List<String> arrayComparator, String stringComparator, String failMessage) {
        if (arrayComparator.contains(stringComparator)) {
            log.debug(failMessage.replace("did not ", "did "));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Assert not null with custom message.
     */
    public static void assertNotNull(Object object, String failMessage) {
        if (object == null) {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Assert null with custom message.
     */
    public static void assertNull(Object object, String failMessage) {
        if (object != null) {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Verbose true assertion that logs before failing.
     */
    public static void verboseTrue(boolean condition, String failMessage) {
        if (condition) {
            log.debug(failMessage.replace("is not", "is"));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }

    /**
     * Verbose false assertion that logs before failing.
     */
    public static void verboseFalse(boolean condition, String failMessage) {
        if (!condition) {
            log.debug(failMessage.replace("is", "is not"));
        } else {
            log.error(failMessage);
            Assert.fail(failMessage);
        }
    }
}
