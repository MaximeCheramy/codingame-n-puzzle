package com.codingame.game;

public enum Image {
    CODEBUSTERS("Codebusters.jpg"), TGE("TGE.jpg"), CSB("CSB.jpg");
    
    private String filename;
    
    Image(String filename) {
        this.filename = filename;
    }
    
    public String getFilename() {
        return filename;
    }
}
