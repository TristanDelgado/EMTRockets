package org.SwEng.subsystems.helpers;

/**
 * Represents an internal system message intended for communication between subsystems.
 * <p>
 * Each instance of this class contains a target {@link Subsystems} identifier and
 * an associated message string.
 * </p>
 */
public class InternalSystemMessaging {

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
    public InternalSystemMessaging(Subsystems subsystem, String message) {
        this.subsystem = subsystem;
        this.message = message;
    }
}
