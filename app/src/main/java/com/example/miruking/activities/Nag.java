package com.example.miruking.activities;


// 데이터 모델담당. DAO와 UI 모두에서 사용
public class Nag {
    private int id;
    private String text;

    public Nag(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
