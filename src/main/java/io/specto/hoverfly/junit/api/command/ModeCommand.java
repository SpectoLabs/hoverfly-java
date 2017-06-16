package io.specto.hoverfly.junit.api.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.core.HoverflyMode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModeCommand {

    private HoverflyMode mode;
    private ModeArguments arguments;

    public ModeCommand() {
    }

    public ModeCommand(HoverflyMode mode) {
        this.mode = mode;
    }

    public ModeCommand(HoverflyMode mode, ModeArguments arguments) {
        this.mode = mode;
        this.arguments = arguments;
    }

    public HoverflyMode getMode() {
        return mode;
    }

    public void setMode(HoverflyMode mode) {
        this.mode = mode;
    }

    public ModeArguments getArguments() {
        return arguments;
    }

    public void setArguments(ModeArguments arguments) {
        this.arguments = arguments;
    }
}
