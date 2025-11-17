package org.SwEng.subsystems.helpers;

import java.util.List;

/**
 * Represents an internal system message intended for communication between subsystems.
 * <p>
 * Each instance of this class contains a target {@link Subsystems} identifier and
 * an associated message string.
 * </p>
 */
public class InternalSystemMessage {

    /**
     * The subsystem that this message is directed to.
     */
    public Subsystems subsystem;

    /**
     * The message content to be sent to the subsystem.
     */
    public String message;

    /**
     * Constructs a new {@code InternalSystemMessaging} object.
     *
     * @param subsystem the target subsystem for this message
     * @param message the message content to be sent
     */
    public InternalSystemMessage(Subsystems subsystem, String message) {
        this.subsystem = subsystem;
        this.message = message;
    }
}
