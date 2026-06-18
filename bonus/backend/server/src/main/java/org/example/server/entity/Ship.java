package org.example.server.entity;

import lombok.Getter;
import org.example.server.entity.enums.ShipOrientation;

@Getter
public class Ship {
    private int size;
    private final int xStart;
    private final int yStart;
    private final ShipOrientation shipOrientation;
    private int hp;

    public Ship(int size, int xStart, int yStart, ShipOrientation shipOrientation) {
        setSize(size);
        int xEnd = xStart + (shipOrientation == ShipOrientation.HORIZONTAL ? size - 1 : 0);
        if (xStart < 0 || xEnd >= Board.BOARD_SIZE)
            throw new IllegalArgumentException("Ship out of board by x, start - " + xStart + " , end - " + xEnd);
        this.xStart = xStart;
        int yEnd = yStart + (shipOrientation == ShipOrientation.VERTICAL ? size - 1 : 0);
        if (yStart < 0 || yEnd >= Board.BOARD_SIZE)
            throw new IllegalArgumentException("Ship out of board by y, start - " + yStart + " , end - " + yEnd);
        this.yStart = yStart;
        this.shipOrientation = shipOrientation;
        this.hp = size;
    }

    private void setSize(int size) {
        if (size <= 0 || size > 4) throw new IllegalArgumentException("size must be in range [1, 4]");
        this.size = size;
    }

    public int[][] getCoord() {
        int[][] coord = new int[size][2];
        int x = xStart;
        int y = yStart;
        int dx = shipOrientation == ShipOrientation.VERTICAL ? 0 : 1;
        int dy = 1 - dx;
        for (int i = 0; i < size; i++) {
            coord[i][0] = x;
            coord[i][1] = y;
            x += dx;
            y += dy;
        }
        return coord;
    }

    public boolean applyDamage() {
        if (alive()) hp--;
        return alive();
    }

    public boolean alive() {
        return hp > 0;
    }
}
