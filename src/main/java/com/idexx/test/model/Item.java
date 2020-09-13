package com.idexx.test.model;

import lombok.Data;

@Data
public class Item {

    private String title;
    private String[] authors;
    private String artist;
    boolean isBook;
    boolean isAlbum;
}
