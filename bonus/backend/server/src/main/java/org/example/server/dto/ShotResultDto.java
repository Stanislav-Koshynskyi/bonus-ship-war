package org.example.server.dto;

import lombok.Getter;
import org.example.server.entity.ShotResult;
import org.example.server.entity.enums.ShotResultType;

import java.util.List;

@Getter
public class ShotResultDto {
    private final ShotResultType result;
    private final List<CellDto> changedCells;
    private final PlayerDto winner;

    public ShotResultDto(ShotResult shotResult) {
        this.result = shotResult.getShotResultType();
        this.changedCells = shotResult.getChangedCells() != null
                ? shotResult.getChangedCells().stream().map(CellDto::new).toList()
                : List.of();
        this.winner = shotResult.getWinner() != null
                ? new PlayerDto(shotResult.getWinner())
                : null;
    }
}
