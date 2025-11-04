package org.SwEng.headCoordinatorSystems;

// Interface for all command handlers
interface CommandHandler {
    void handle(String[] args);
    String getCommandName();
    String getDescription();
}