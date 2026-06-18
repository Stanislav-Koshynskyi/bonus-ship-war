package org.example.server.entity;

import org.example.server.entity.enums.CellState;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class Board {
    public static int BOARD_SIZE = 10;
    private static int SHIPS_LENGTH = 10;
    public static final int[] MAX_SHIP_COUNT_PER_LENGTH = {4, 3, 2, 1};
    private final List<Ship> ships;
    private final CellState[][] board = new CellState[BOARD_SIZE][BOARD_SIZE];

    public Board(List<Ship> ships) {
        if (ships.size() != SHIPS_LENGTH) throw new IllegalArgumentException("Must be " + SHIPS_LENGTH + " ships");
        for (int i = 0; i < MAX_SHIP_COUNT_PER_LENGTH.length; i++) {
            int finalI = i;
            int len = finalI + 1;
            if (ships.stream().filter(s -> s.getSize() == len).toList().size() != MAX_SHIP_COUNT_PER_LENGTH[finalI])
                throw new IllegalArgumentException("Too many/few sheep with length: " + len);
        }
        this.ships = ships;
        placeShips();

    }

    private void placeShips() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = CellState.VOID;
            }
        }
        for (Ship ship : ships) {
            Point[] coord = ship.getCoord();
            for (Point point : coord) {
                board[point.x][point.y] = CellState.SHIP;
            }
        }
    }

    public CellState getCellState(int x, int y) {
        return board[x][y];
    }

    public void setCellState(int x, int y, CellState state) {
        if ((x >= 0 && x < board.length)&&(y >= 0 && y < board[0].length))
            board[x][y] = state;
    }

    public Ship getShipAt(int x, int y) {
        for (Ship ship : ships) {
            if (Arrays.stream(ship.getCoord()).toList().contains(new Point(x, y))) {
                return ship;
            }
        }
        return null;
    }

    public boolean defeat() {
        for (Ship ship : ships) {
            if (ship.alive()) {
                return false;
            }
        }
        return true;
    }

}
