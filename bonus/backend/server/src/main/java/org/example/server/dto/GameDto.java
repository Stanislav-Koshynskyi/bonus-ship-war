package org.example.server.dto;

import lombok.Getter;
import org.example.server.entity.Game;
import org.example.server.entity.enums.GameState;

@Getter
public class GameDto {
    private final String id;
    private final GameState state;
    private final PlayerDto player1;
    private final PlayerDto player2;
    private final String whoTurnNowId;

    public GameDto(Game game) {
        this.id = game.getId();
        this.state = game.getState();
        this.player1 = new PlayerDto(game.getPlayer1());
        this.player2 = game.getPlayer2() != null ? new PlayerDto(game.getPlayer2()) : null;
        this.whoTurnNowId = game.getWhoTurnNow() != null ? game.getWhoTurnNow().getId() : null;
    }
}
