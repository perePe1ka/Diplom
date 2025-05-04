package ru.vladuss.apigateway.constant;

public enum StatementType {
    AID,
    TICKET;

    public String display() {
        return switch (this) {
            case AID    -> "Материальная помощь";
            case TICKET -> "Вступление в Профсоюз";
        };
    }
}
