package server.messages;

import model.Ship;

import java.io.Serializable;

public class MoveResponseMessage implements Serializable {

    private int x;
    private int y;
    private Ship shipSunk;
    private boolean hit;
    private boolean ownBoard;

    public MoveResponseMessage(int x, int y, boolean hit, boolean ownBoard) {
        this(x, y, null, hit, ownBoard);
    }
    public MoveResponseMessage(int x, int y, Ship shipSunk, boolean hit,
            boolean ownBoard) {
        this.x = x;
        this.y = y;
        this.shipSunk = shipSunk;
        this.hit = hit;
        this.ownBoard = ownBoard;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public Ship shipSank() {
        return this.shipSunk;
    }
    public boolean isHit() {
        return hit;
    }
    public boolean isOwnBoard() {
        return ownBoard;
    }
    public void setOwnBoard(boolean ownBoard) {
        this.ownBoard = ownBoard;
    }

}
