package com.aikichun.domain.progress.engine;

import com.aikichun.domain.progress.entity.TaskProgressStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicProgressEngineTest {

    @Test
    @DisplayName("Should return 0% and NOT_STARTED when all checkboxes are false")
    void testAllFalse() {
        BigDecimal percentage = DynamicProgressEngine.calculateTaskProgressPercentage(false, false, false, false);
        TaskProgressStatus status = DynamicProgressEngine.deriveStatus(false, false, false, false);

        assertEquals(new BigDecimal("0.00"), percentage);
        assertEquals(TaskProgressStatus.NOT_STARTED, status);
    }

    @Test
    @DisplayName("Should return 25% and IN_PROGRESS when 1 checkbox is true")
    void testOneChecked() {
        BigDecimal percentage = DynamicProgressEngine.calculateTaskProgressPercentage(true, false, false, false);
        TaskProgressStatus status = DynamicProgressEngine.deriveStatus(true, false, false, false);

        assertEquals(new BigDecimal("25.00"), percentage);
        assertEquals(TaskProgressStatus.IN_PROGRESS, status);
    }

    @Test
    @DisplayName("Should return 50% and IN_PROGRESS when 2 checkboxes are true")
    void testTwoChecked() {
        BigDecimal percentage = DynamicProgressEngine.calculateTaskProgressPercentage(true, true, false, false);
        TaskProgressStatus status = DynamicProgressEngine.deriveStatus(true, true, false, false);

        assertEquals(new BigDecimal("50.00"), percentage);
        assertEquals(TaskProgressStatus.IN_PROGRESS, status);
    }

    @Test
    @DisplayName("Should return 75% and IN_PROGRESS when 3 checkboxes are true")
    void testThreeChecked() {
        BigDecimal percentage = DynamicProgressEngine.calculateTaskProgressPercentage(true, true, true, false);
        TaskProgressStatus status = DynamicProgressEngine.deriveStatus(true, true, true, false);

        assertEquals(new BigDecimal("75.00"), percentage);
        assertEquals(TaskProgressStatus.IN_PROGRESS, status);
    }

    @Test
    @DisplayName("Should return 100% and COMPLETED when all 4 checkboxes are true")
    void testAllChecked() {
        BigDecimal percentage = DynamicProgressEngine.calculateTaskProgressPercentage(true, true, true, true);
        TaskProgressStatus status = DynamicProgressEngine.deriveStatus(true, true, true, true);

        assertEquals(new BigDecimal("100.00"), percentage);
        assertEquals(TaskProgressStatus.COMPLETED, status);
    }
}

