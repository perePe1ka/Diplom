package ru.vladuss.documentgeneration.constants;

public enum StatementType {
    AID,
    TICKET;

    public String display() {
        return switch (this) {
            case AID    -> "Матпомощь";
            case TICKET -> "Билет";
        };
    }
}
