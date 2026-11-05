package com.aikichun.domain.progress.engine;

import com.aikichun.domain.progress.entity.TaskProgressStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DynamicProgressEngine {

    private DynamicProgressEngine() {
    }

    public static BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0 || part == null || part.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return part.divide(total, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTaskProgressPercentage(int completedChecks, int totalChecks) {
        if (totalChecks <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (completedChecks <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (completedChecks >= totalChecks) {
            return BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf((double) completedChecks / (double) totalChecks * 100.0)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static TaskProgressStatus deriveStatus(int completedChecks, int totalChecks) {
        if (totalChecks <= 0 || completedChecks <= 0) {
            return TaskProgressStatus.NOT_STARTED;
        }
        if (completedChecks >= totalChecks) {
            return TaskProgressStatus.COMPLETED;
        }
        return TaskProgressStatus.IN_PROGRESS;
    }

    public static BigDecimal calculateTaskProgressPercentage(boolean knowledge, boolean practice, boolean pressure, boolean application) {
        int checkedCount = 0;
        if (knowledge) checkedCount++;
        if (practice) checkedCount++;
        if (pressure) checkedCount++;
        if (application) checkedCount++;
        return calculateTaskProgressPercentage(checkedCount, 4);
    }

    public static TaskProgressStatus deriveStatus(boolean knowledge, boolean practice, boolean pressure, boolean application) {
        int checkedCount = 0;
        if (knowledge) checkedCount++;
        if (practice) checkedCount++;
        if (pressure) checkedCount++;
        if (application) checkedCount++;
        return deriveStatus(checkedCount, 4);
    }
}
