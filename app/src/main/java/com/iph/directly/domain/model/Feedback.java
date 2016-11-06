package com.iph.directly.domain.model;

/**
 * Created by vanya on 11/5/2016.
 */

public class Feedback {
    private String text;

    public Feedback(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
