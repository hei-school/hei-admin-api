package school.hei.haapi.model;

public enum InterestTimeRate {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY");

    private final String text;

    InterestTimeRate(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static InterestTimeRate getInterestTimeRate(String text) {
        for (InterestTimeRate itr : InterestTimeRate.values()) {
            if (itr.text.equalsIgnoreCase(text)) {
                return itr;
            }
        }
        return null;
    }
}

