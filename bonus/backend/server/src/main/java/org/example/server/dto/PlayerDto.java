package org.example.server.dto;

import lombok.Getter;
import org.example.server.entity.Player;
import org.example.server.entity.enums.PlayerState;

@Getter
public class PlayerDto {
    private final String id;
    private final String nickName;
    private final PlayerState state;

    public PlayerDto(Player player) {
        this.id = player.getId();
        this.nickName = player.getNickName();
        this.state = player.getState();
    }
}
