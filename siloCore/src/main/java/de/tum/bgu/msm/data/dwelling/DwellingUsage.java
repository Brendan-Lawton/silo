package de.tum.bgu.msm.data.dwelling;

public enum DwellingUsage {
    GROUP_QUARTER_OR_DEFAULT, OWNED, RENTED, VACANT;

    public static DwellingUsage valueOf(int code) {
        switch (code) {
            case -1:
                return GROUP_QUARTER_OR_DEFAULT;
            case 1:
                return OWNED;
            case 2:
                return OWNED;
            case 3:
                return RENTED;
            case 4:
                return RENTED;
            case 5:
                return VACANT;
            default:
                throw new RuntimeException("Undefined dwelling usage code " + code);
        }
    }
}
