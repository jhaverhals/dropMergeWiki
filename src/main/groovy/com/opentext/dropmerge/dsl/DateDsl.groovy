package com.opentext.dropmerge.dsl

class DateDsl {
    private Calendar today = Calendar.getInstance()
    private Boolean nextOdd = null
    private boolean includeToday = true
    private int day = today.get(Calendar.DAY_OF_WEEK);

    public DateDsl setIncludeToday(boolean b) {
        includeToday = b
        return this
    }

    public DateDsl getOrNextOdd() {
        nextOdd = true
        return this
    }

    public DateDsl getOrNextEven() {
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
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int daysUntilNextTargetDOW = day - dayOfWeek
        if (daysUntilNextTargetDOW < 0 || (daysUntilNextTargetDOW == 0 && !includeToday)) {
            daysUntilNextTargetDOW = daysUntilNextTargetDOW + 7
        }

        Calendar nextTargetDOW = (Calendar) today.clone()
        nextTargetDOW.add(Calendar.DAY_OF_WEEK, daysUntilNextTargetDOW)
        if (nextOdd != null) {
            if (nextTargetDOW.get(Calendar.WEEK_OF_YEAR) % 2 != (nextOdd.booleanValue() ? 1 : 0)) {
                nextTargetDOW.add(Calendar.DAY_OF_WEEK, 7)
            }
        }

        return nextTargetDOW.getTime()
    }
}
