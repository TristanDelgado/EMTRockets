package org.SwEng.subsystems.helpers;

/**
 * Provides a common area to track all existing subsystems
 * within the application architecture.
 *
 * <p>
 * Each enum constant represents a distinct, high-level
 * component or service within the overall system.
 * </p>
 *
 * @author [Your Name or Team Name]
 * @version 1.0
 * @since [Date or Version when introduced, e.g., 2025-10-31 or 1.0]
 */
public enum Subsystems {
    /**
     * Represents the **Account Management** subsystem, responsible for
     * user authentication, authorization, profiles, and configuration.
     */
    ACCOUNT_SERVICE,

    /**
     * Represents the **Messaging and Notification** subsystem, responsible for
     * handling internal and external communication (e.g., email, push notifications).
     */
    MESSAGING_SERVICE,

    /**
     * Represents the core **System and Utility** subsystem, responsible for
     * centralized logging, configuration, health checks, and scheduled tasks.
     */
    SYSTEM_SERVICE,

    /**
     * Represents the **E-commerce/Storefront** subsystem, responsible for
     * product catalog, inventory, order processing, and payment integration.
     */
    STORE_SERVICE;
}
