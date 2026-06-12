package org.example.server.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.server.entity.enums.PlayerState;

import java.util.UUID;


@Getter
@Setter
@EqualsAndHashCode
public class Player {
    private String id;
    private PlayerState state;
    private String nickName;
    public Player(String nickName){
        id = UUID.randomUUID().toString();
        this.nickName = nickName;
        state = PlayerState.OUT_OF_GAME;
    }
}
