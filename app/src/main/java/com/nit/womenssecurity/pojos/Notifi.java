package com.nit.womenssecurity.pojos;

import java.io.Serializable;

public class Notifi implements Serializable {

    private String id;
    private String receiverId;
    private String senderId;
    private long time;
    private String title;
    private String category;
    private String body;
    private boolean seen;

    public Notifi() {
    }

    public Notifi(String id, String receiverId, String senderId, long time, String title, String category, String body, boolean seen) {
        this.id = id;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.time = time;
        this.title = title;
        this.category = category;
        this.body = body;
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public String toString() {
        return "Notifi{" +
                "id='" + id + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", time=" + time +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", body='" + body + '\'' +
                ", seen=" + seen +
                '}';
    }
}
