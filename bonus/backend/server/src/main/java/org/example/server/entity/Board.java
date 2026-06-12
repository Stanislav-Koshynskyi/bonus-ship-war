package org.example.server.entity;

import org.example.server.entity.enums.CellState;

import java.util.List;

public class Board {
    public static int BOARD_SIZE = 10;
    private static int SHIPS_LENGTH = 10;
    private static int[] maxShipCountPerLenght = {4, 3, 2, 1};
    private final List<Ship> ships;
    private final CellState[][] board = new CellState[BOARD_SIZE][BOARD_SIZE];

    public Board (List<Ship> ships){
        if (ships.size() != SHIPS_LENGTH) throw new IllegalArgumentException("Must be " + SHIPS_LENGTH + " ships");
        for (int i = 0; i < maxShipCountPerLenght.length; i++){
            int finalI = i;
            int len = finalI + 1;
            if (ships.stream().filter(s -> s.getSize() == len).toList().size() != maxShipCountPerLenght[finalI])
                throw new IllegalArgumentException("Too many/few sheep with length: " + len);
        }
        this.ships = ships;
        placeShips();

    }
    private void placeShips(){
        for (int i = 0; i < BOARD_SIZE; i++){
            for (int j = 0; j< BOARD_SIZE; j ++){
                board[i][j] = CellState.VOID;
            }
        }
        for (Ship ship: ships){
            int[][] coord = ship.getCoord();
            for (int[] point : coord){
                board[point[0]][point[1]] = CellState.SHIP;
            }
        }
    }

}
