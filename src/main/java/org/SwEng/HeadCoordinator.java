package org.SwEng;


import java.util.*;

// Interface for all command handlers
interface CommandHandler {
    void handle(String[] args);
    String getCommandName();
    String getDescription();
}

// Example handler implementations
class HelpHandler implements CommandHandler {
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

class UserHandler implements CommandHandler {
    @Override
    public void handle(String[] args) {
        if (args.length > 0) {
            System.out.println("Processing user operation: " + String.join(" ", args));
            // Add your user management logic here
        } else {
            System.out.println("Usage: user <operation> [arguments]");
        }
    }

    @Override
    public String getCommandName() { return "user"; }

    @Override
    public String getDescription() { return "Manage user operations"; }
}

class DataHandler implements CommandHandler {
    @Override
    public void handle(String[] args) {
        if (args.length > 0) {
            System.out.println("Processing data operation: " + String.join(" ", args));
            // Add your data processing logic here
        } else {
            System.out.println("Usage: data <operation> [arguments]");
        }
    }

    @Override
    public String getCommandName() { return "data"; }

    @Override
    public String getDescription() { return "Manage data operations"; }
}

class ExitHandler implements CommandHandler {
    @Override
    public void handle(String[] args) {
        System.out.println("Shutting down...");
        System.exit(0);
    }

    @Override
    public String getCommandName() { return "exit"; }

    @Override
    public String getDescription() { return "Exit the application"; }
}

// Main Head Coordinator class
public class HeadCoordinator {
    private Map<String, CommandHandler> handlers;
    private Scanner scanner;

    public HeadCoordinator() {
        handlers = new HashMap<>();
        scanner = new Scanner(System.in);
        initializeHandlers();
    }

    private void initializeHandlers() {
        // Register all command handlers
        registerHandler(new UserHandler());
        registerHandler(new DataHandler());
        registerHandler(new ExitHandler());
        registerHandler(new HelpHandler(handlers));
    }

    private void registerHandler(CommandHandler handler) {
        handlers.put(handler.getCommandName().toLowerCase(), handler);
    }

    public void processCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+");
        String command = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        CommandHandler handler = handlers.get(command);

        if (handler != null) {
            try {
                handler.handle(args);
            } catch (Exception e) {
                System.err.println("Error executing command: " + e.getMessage());
            }
        } else {
            System.out.println("Unknown command: " + command);
            System.out.println("Type 'help' for available commands.");
        }
    }

    public void start() {
        System.out.println("Head Coordinator Started");
        System.out.println("Type 'help' for available commands\n");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            processCommand(input);
        }
    }

//    public static void main(String[] args) {
//        HeadCoordinator coordinator = new HeadCoordinator();
//        coordinator.start();
//    }
}