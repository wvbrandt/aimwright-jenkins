package com.spectralink.aimwright.helpers;

import ch.qos.logback.classic.Logger;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.AmieOptions;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Playwright helper for interacting with HTML tables.
 * Provides methods for data extraction, sorting, and cell navigation.
 *
 * Uses Playwright's Locator API with built-in auto-wait.
 */
public class TableHelper {
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

    private final Page page;
    private final Locator tableLocator;
    private final Locator headers;
    private final ArrayList<String> headerValues = new ArrayList<>();

    // Default selectors for AMiE tables
    private static final String DEFAULT_TABLE_SELECTOR = "#main_content table";
    private static final String HEADER_SELECTOR = "th";
    private static final String ROW_SELECTOR = "tbody tr";
    private static final String CELL_SELECTOR = "td";
    private static final String SORTER_SELECTOR = ".ant-table-column-sorter";
    private static final String SORTER_UP_SELECTOR = ".ant-table-column-sorter-up";
    private static final String SORTER_DOWN_SELECTOR = ".ant-table-column-sorter-down";

    /**
     * Creates a TableHelper for the default table in main content area.
     */
    public TableHelper(Page page) {
        this.page = page;
        this.tableLocator = page.locator(DEFAULT_TABLE_SELECTOR);
        this.headers = tableLocator.locator(HEADER_SELECTOR);
        initHeaderValues();
    }

    /**
     * Creates a TableHelper for a table matching the given selector.
     */
    public TableHelper(Page page, String tableSelector) {
        this.page = page;
        Locator element = page.locator(tableSelector);
        // If selector doesn't point to table directly, find table within
        if (!tableSelector.contains("table")) {
            this.tableLocator = element.locator("table").first();
        } else {
            this.tableLocator = element;
        }
        this.headers = tableLocator.locator(HEADER_SELECTOR);
        initHeaderValues();
    }

    /**
     * Initialize header values from the table.
     */
    private void initHeaderValues() {
        List<String> texts = headers.allTextContents();
        headerValues.addAll(texts);
        log.debug("Table headers: {}", headerValues);
    }

    /**
     * Returns all row locators in the table body.
     */
    public Locator rows() {
        return tableLocator.locator(ROW_SELECTOR);
    }

    /**
     * Returns a specific row by index.
     */
    public Locator row(int index) {
        return rows().nth(index);
    }

    /**
     * Returns the number of columns.
     */
    public int getColumnCount() {
        return headers.count();
    }

    /**
     * Returns the number of data rows.
     */
    public int getRowCount() {
        return rows().count();
    }

    /**
     * Returns header names (excluding empty headers).
     */
    public List<String> getHeaderNames() {
        List<String> cleanHeaders = new ArrayList<>(headerValues);
        cleanHeaders.removeAll(Collections.singleton(""));
        cleanHeaders.removeAll(Collections.singleton(null));
        return cleanHeaders;
    }

    /**
     * Returns a cell locator by row index and column position.
     */
    public Locator cell(int rowIndex, int columnIndex) {
        return row(rowIndex).locator(CELL_SELECTOR).nth(columnIndex);
    }

    /**
     * Returns a cell locator by row index and column name.
     */
    public Locator cell(int rowIndex, String columnName) {
        int colIndex = headerValues.indexOf(columnName);
        if (colIndex < 0) {
            log.warn("Column '{}' not found in headers: {}", columnName, headerValues);
            return null;
        }
        return cell(rowIndex, colIndex);
    }

    /**
     * Finds and returns a cell containing the specified value in the given column.
     */
    public Locator getCellWithValue(String value, String columnName) {
        int colIndex = headerValues.indexOf(columnName);
        if (colIndex < 0) {
            log.warn("Column '{}' not found", columnName);
            return null;
        }

        List<String> columnValues = getColumnValues(columnName);
        int rowIndex = columnValues.indexOf(value);
        if (rowIndex < 0) {
            log.warn("Value '{}' not found in column '{}'", value, columnName);
            return null;
        }

        return cell(rowIndex, colIndex);
    }

    /**
     * Returns the row index of a cell containing the specified value.
     */
    public int getRowIndexOfCellWithValue(String value, String columnName) {
        if (!headerValues.contains(columnName)) {
            log.warn("Cannot find column named '{}'", columnName);
            return -1;
        }

        List<String> columnValues = getColumnValues(columnName);
        log.debug("Column values: {}", columnValues);
        return columnValues.indexOf(value);
    }

    /**
     * Returns all values in a specific row.
     */
    public List<String> getRowValues(int rowIndex) {
        List<String> values = new ArrayList<>();
        if (getRowCount() == 0) {
            log.warn("Table is empty");
            return values;
        }

        Locator cells = row(rowIndex).locator(CELL_SELECTOR);
        values.addAll(cells.allTextContents());
        log.trace("Row {} values: {}", rowIndex, values);
        return values;
    }

    /**
     * Returns all values in a specific column.
     */
    public List<String> getColumnValues(String columnName) {
        List<String> values = new ArrayList<>();
        int colIndex = headerValues.indexOf(columnName);

        if (colIndex < 0) {
            log.warn("Column '{}' not found", columnName);
            return values;
        }

        if (getRowCount() == 0) {
            log.warn("Table is empty");
            return values;
        }

        // Use CSS nth-child selector for efficient column extraction
        Locator columnCells = tableLocator.locator(ROW_SELECTOR + " " + CELL_SELECTOR + ":nth-child(" + (colIndex + 1) + ")");
        values.addAll(columnCells.allTextContents());
        log.trace("Column '{}' values: {}", columnName, values);
        return values;
    }

    /**
     * Gets the current sort state of a column.
     */
    private AmieOptions.Sort getCurrentSorting(Locator sorterElement) {
        Locator ascCaret = sorterElement.locator(SORTER_UP_SELECTOR);
        Locator descCaret = sorterElement.locator(SORTER_DOWN_SELECTOR);

        String ascClass = ascCaret.getAttribute("class");
        String descClass = descCaret.getAttribute("class");

        if (ascClass != null && ascClass.contains("on")) {
            return AmieOptions.Sort.ASC;
        } else if (descClass != null && descClass.contains("on")) {
            return AmieOptions.Sort.DESC;
        }
        return AmieOptions.Sort.NONE;
    }

    /**
     * Sorts the table by the specified column.
     * Clicks the sorter up to 3 times to achieve the desired sort order.
     */
    public void sort(String columnName, AmieOptions.Sort sortOrder) {
        int colIndex = headerValues.indexOf(columnName);
        if (colIndex < 0) {
            log.error("Column '{}' not found for sorting", columnName);
            return;
        }

        Locator headerCell = headers.nth(colIndex);
        Locator sorter = headerCell.locator(SORTER_SELECTOR);

        if (sorter.count() == 0) {
            log.error("No sorter element found for column: {}", columnName);
            return;
        }

        // Click up to 3 times to cycle through sort states
        for (int i = 0; i < 3; i++) {
            AmieOptions.Sort currentSort = getCurrentSorting(sorter);
            if (currentSort == sortOrder) {
                log.debug("Column '{}' is now sorted {}", columnName, sortOrder);
                break;
            }
            sorter.click();
        }
    }

    /**
     * Returns a string representation of all table data.
     */
    @Override
    public String toString() {
        List<List<String>> allRows = new ArrayList<>();
        int rowCount = getRowCount();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            allRows.add(getRowValues(rowIndex));
        }
        return allRows.toString();
    }
}
