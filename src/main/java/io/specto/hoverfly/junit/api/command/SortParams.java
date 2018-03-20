package io.specto.hoverfly.junit.api.command;

public class SortParams {

    private final String property;
    private final Direction direction;

    public SortParams(String property, Direction direction) {
        this.property = property;
        this.direction = direction == null ? Direction.ASC : direction;
    }

    public String getProperty() {
        return property;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return String.join(":", this.property, this.direction.name());
    }

    public enum Direction {
        ASC, DESC
    }
}


