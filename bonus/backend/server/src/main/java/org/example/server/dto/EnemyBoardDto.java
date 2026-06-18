package org.example.server.dto;

import lombok.Getter;
import org.example.server.entity.Board;
import org.example.server.entity.enums.CellState;

@Getter
public class EnemyBoardDto {
    private final CellState[][] cells;

    /** Якщо board == null (гра ще не IN_PROGRESS) — вся сітка VOID. */
    public EnemyBoardDto(Board board) {
        int size = Board.BOARD_SIZE;
        this.cells = new CellState[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                this.cells[x][y] = board != null
                        ? mask(board.getCellState(x, y))
                        : CellState.VOID;
            }
        }
    }

    private CellState mask(CellState state) {
        return switch (state) {
            case MISS, ATTACKED_SHIP_PART, DEAD_SHIP -> state;
            default -> CellState.VOID;
        };
    }
}
