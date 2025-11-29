package org.SwEng.subsystems.general;

public enum AccountType {

    CUSTOMER("customer"),
    WORKER("worker"),
    CEO("ceo");

    private final String stringValue;

    AccountType(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Converts a string value to the corresponding AccountType enum constant.
     *
     * @param stringValue The string value to convert.
     * @return The AccountType enum constant.
     * @throws IllegalArgumentException if the string does not match any enum constant's stringValue.
     */
    public static AccountType fromString(String stringValue) {
        for (AccountType type : AccountType.values()) {
            if (type.stringValue.equalsIgnoreCase(stringValue.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with string value: " + stringValue);
    }

    public String getStringValue() {
        return stringValue;
    }
}
