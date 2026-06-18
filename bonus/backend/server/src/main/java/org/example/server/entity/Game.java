package org.example.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.server.entity.enums.GameState;
import org.example.server.entity.enums.PlayerState;

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
    }
    public boolean everyoneReady(){
        return (player1 != null && player2 != null
                && player1.getState() == PlayerState.READY && player2.getState() == PlayerState.READY);
    }
    public boolean playerInGame(Player player){
        return (player.equals(player1) || player.equals(player2));
    }
    public boolean playerHasBoard(Player player){
        if (player.equals(player1)){
            return player1Board != null;
        }
        if (player.equals(player2)){
            return  player2Board != null;
        }
        return false;
    }
    public Board getOpponentBoard(Player player){
        if (state != GameState.IN_PROGRESS) return null;
        if (player.equals(player1)){
            return player2Board;
        }
        if (player.equals(player2)){
            return  player1Board;
        }
        return null;
    }
    public void passTurn(){
        whoTurnNow = whoTurnNow == player1 ? player2 : player1;
    }
    public Board getPlayerBoard(Player player){
        if (player.equals(player1)) return player1Board;
        if (player.equals(player2)) return player2Board;
        return null;
    }
}
