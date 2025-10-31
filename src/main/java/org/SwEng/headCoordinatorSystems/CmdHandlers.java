package org.SwEng.headCoordinatorSystems;

import java.util.Map;

public class CmdHandlers {
    // Example handler implementations
    static class HelpHandler implements CommandHandler {
        private Map<String, CommandHandler> handlers;

        public HelpHandler(Map<String, CommandHandler> handlers) {
            this.handlers = handlers;
        }

        @Override
        public void handle(String[] args) {
            System.out.println("\nAvailable commands:");
            for (CommandHandler handler : handlers.values()) {
                System.out.println("  " + handler.getCommandName() + " - " + handler.getDescription());
            }
        }

        @Override
        public String getCommandName() { return "help"; }

        @Override
        public String getDescription() { return "Display available commands"; }
    }


}
