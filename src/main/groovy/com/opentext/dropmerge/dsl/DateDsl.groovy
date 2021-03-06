package com.opentext.dropmerge.dsl

import static java.util.Calendar.*

class DateDsl {
    private Calendar today = Calendar.getInstance()
    private Boolean nextOdd = null
    private boolean includeToday = true
    private int day = today[DAY_OF_WEEK]

    public DateDsl setIncludeToday(boolean b) {
        includeToday = b
        return this
    }

    public DateDsl getIncludingToday() {
        includeToday = true
        return this
    }

    public DateDsl getExcludingToday() {
        includeToday = false
        return this
    }

    public DateDsl getOdd() {
        nextOdd = true
        return this
    }

    public DateDsl getEven() {
        nextOdd = false
        return this
    }

    public DateDsl getSunday() {
        this.day = Calendar.SUNDAY
        return this
    }

    public DateDsl getMonday() {
        this.day = Calendar.MONDAY
        return this
    }

    public DateDsl getTuesday() {
        this.day = Calendar.TUESDAY
        return this
    }

    public DateDsl getWednesday() {
        this.day = Calendar.WEDNESDAY
        return this
    }

    public DateDsl getThursday() {
        this.day = Calendar.THURSDAY
        return this
    }

    public DateDsl getFriday() {
        this.day = Calendar.FRIDAY
        return this
    }

    public DateDsl getSaturday() {
        this.day = Calendar.SATURDAY
        return this
    }

    Date getDate() {
        int dayOfWeek = today.get(DAY_OF_WEEK);
        int daysUntilNextTargetDOW = day - dayOfWeek
        if (daysUntilNextTargetDOW < 0 || (daysUntilNextTargetDOW == 0 && !includeToday)) {
            daysUntilNextTargetDOW = daysUntilNextTargetDOW + 7
        }

        Calendar nextTargetDOW = (Calendar) today.clone()
        nextTargetDOW.add(DAY_OF_WEEK, daysUntilNextTargetDOW)
        if (nextOdd != null) {
            if (nextTargetDOW[WEEK_OF_YEAR] % 2 != (nextOdd.booleanValue() ? 1 : 0)) {
                nextTargetDOW.add(DAY_OF_WEEK, 7)
            }
        }

        return nextTargetDOW.getTime()
    }
}
