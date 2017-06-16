package io.specto.hoverfly.junit.api.command;

public class DestinationCommand {

    private String destination;

    public DestinationCommand() {
    }

    public DestinationCommand(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
