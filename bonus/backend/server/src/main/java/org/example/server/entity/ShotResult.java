package org.example.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.server.entity.enums.ShotResultType;

import java.util.List;
@Getter
@Setter
public class ShotResult {
    private List<Cell> changedCells;
    private ShotResultType shotResultType;
    private Player winner;

    public ShotResult(List<Cell> changedCells, ShotResultType shotResultType) {
        this.changedCells = changedCells;
        this.shotResultType = shotResultType;
    }
    public ShotResult(){}
}
