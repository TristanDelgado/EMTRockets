package main.java.org.SwEng.storeSystem;

public class Message {private static int idCounter = 1;
    private int id;
    private String from;
    private String content;
    private boolean read;

    public Message(String var1, String var2) {
        this.id = idCounter++;
        this.from = var1;
        this.content = var2;
        this.read = false;
    }

    public int getId() {
        return this.id;
    }

    public String getFrom() {
        return this.from;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isRead() {
        return this.read;
    }

    public void markRead() {
        this.read = true;
    }

    public String toString() {
        return "Message ID: " + this.id + " From: " + this.from + " - " + (this.read ? "[Read] " : "[Unread] ") + this.content;
    }
}
