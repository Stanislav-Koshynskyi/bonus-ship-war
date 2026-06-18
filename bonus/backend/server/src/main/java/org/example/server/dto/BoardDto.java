package org.example.server.dto;

import lombok.Getter;
import org.example.server.entity.Board;
import org.example.server.entity.enums.CellState;

@Getter
public class BoardDto {
    private final CellState[][] cells;

    public BoardDto(Board board) {
        int size = Board.BOARD_SIZE;
        this.cells = new CellState[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                this.cells[x][y] = board.getCellState(x, y);
            }
        }
    }
}
