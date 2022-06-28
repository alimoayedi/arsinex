package com.arsinex.com.NotificationCenter;

public class NotificationObject {

    private boolean priority = false;

    public NotificationObject(boolean priority) {
        this.priority = priority;
    }

    public boolean hasPriority() {
        return priority;
    }

}
