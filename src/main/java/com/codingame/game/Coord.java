package com.codingame.game;

public class Coord {
    public final int row;
    public final int col;
    
    public Coord(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public String toString() {
        return row + " " + col;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coord) {
            Coord other = (Coord) obj;
            return col == other.col && row == other.row;
        } else {
            return false;
        }
    }
}