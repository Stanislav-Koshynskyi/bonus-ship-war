package org.example.server.dto;

import lombok.Getter;
import org.example.server.entity.Cell;
import org.example.server.entity.enums.CellState;

@Getter
public class CellDto {
    private final int x;
    private final int y;
    private final CellState state;

    public CellDto(Cell cell) {
        this.x = cell.getX();
        this.y = cell.getY();
        this.state = cell.getCellState();
    }
}
