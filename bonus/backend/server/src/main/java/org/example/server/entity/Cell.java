package org.example.server.entity;

import lombok.Getter;
import org.example.server.entity.enums.CellState;

public class Cell{
    @Getter
    private final int x, y;
    @Getter
    private final CellState cellState;

    public Cell(int x, int y, CellState cellState) {
        this.x = x;
        this.y = y;
        this.cellState = cellState;
    }
}
