package org.example.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.server.entity.enums.GameState;

import java.util.UUID;

@Getter
@Setter
public class Game {
    private String id;
    private Player player1;
    private Board player1Board;
    private Player player2;
    private Board player2Board;
    private Player whoTurnNow;
    private GameState state;
    private Player winner;
    public Game(Player player1){
        id = UUID.randomUUID().toString();
        this.player1 = player1;
        whoTurnNow = player1;
        state = GameState.WAITING_FOR_OPPONENT;
    }
    public void join(Player player2){
        if (player1.equals(player2)) throw new IllegalArgumentException("Cant play with yourself");
        this.player2 = player2;
        state = GameState.SHIP_PLACEMENT;
    }
    public void changeTurn(){
        whoTurnNow = whoTurnNow.equals(player1) ? player2 : player1;
    }
    public void setPlayerBoard(Player player, Board board){
        if (player.equals(player1)){
            player1Board = board;
        }
        else if (player.equals(player2)){
            player2Board = board;
        }
        if (player1Board != null && player2Board != null){
            state = GameState.IN_PROGRESS;
        }
    }
}
