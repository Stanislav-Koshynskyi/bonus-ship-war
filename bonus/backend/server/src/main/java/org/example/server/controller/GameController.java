package org.example.server.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.*;
import org.example.server.entity.Board;
import org.example.server.entity.Game;
import org.example.server.entity.Player;
import org.example.server.entity.ShotResult;
import org.example.server.entity.enums.GameState;
import org.example.server.service.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game/create")
    public void createGame(CreateGameRequest request) {
        Game game = gameService.createGame(request.playerId());
        messagingTemplate.convertAndSend(
                "/topic/player/" + request.playerId(),
                new GameDto(game)
        );
    }

    @MessageMapping("/game/join")
    public void joinGame(JoinGameRequest request) {
        Game game = gameService.joinGame(request.gameId(), request.playerId());
        notifyBothPlayers(game, new GameDto(game));

        String p1 = game.getPlayer1().getId();
        String p2 = game.getPlayer2().getId();
        sendPlayerBoard(game, p1);
        sendPlayerBoard(game, p2);
        sendEnemyBoard(game, p1);
        sendEnemyBoard(game, p2);
    }


    @MessageMapping("/game/board/generate")
    public void generateBoard(GenerateBoardRequest request) {
        Board board = gameService.generateBoard(request.gameId(), request.playerId());
        messagingTemplate.convertAndSend(
                "/topic/player/" + request.playerId() + "/board",
                new BoardDto(board)
        );
    }


    @MessageMapping("/game/ready")
    public void ready(ReadyRequest request) {
        Game game = gameService.ready(request.gameId(), request.playerId());
        notifyBothPlayers(game, new GameDto(game));

        if (game.getState() == GameState.IN_PROGRESS) {
            sendEnemyBoard(game, game.getPlayer1().getId());
            sendEnemyBoard(game, game.getPlayer2().getId());
        }
    }


    @MessageMapping("/game/shot")
    public void shot(ShotRequest request) {
        ShotResult result = gameService.processShot(
                request.gameId(), request.playerId(), request.x(), request.y()
        );

        messagingTemplate.convertAndSend(
                "/topic/game/" + request.gameId(),
                new ShotResultDto(result)
        );

        Game game = gameService.getGame(request.gameId());
        if (game != null) {
            String attackerId = request.playerId();
            String defenderId = game.getPlayer1().getId().equals(attackerId)
                    ? game.getPlayer2().getId()
                    : game.getPlayer1().getId();

            sendEnemyBoard(game, attackerId);
            sendPlayerBoard(game, defenderId);
        }
    }

    private void notifyBothPlayers(Game game, Object payload) {
        messagingTemplate.convertAndSend("/topic/game/" + game.getId(), payload);
    }

    private void sendPlayerBoard(Game game, String playerId) {
        Board board = gameService.getPlayerBoard(game.getId(), playerId);
        if (board != null) {
            messagingTemplate.convertAndSend(
                    "/topic/player/" + playerId + "/board",
                    new BoardDto(board)
            );
        }
    }

    private void sendEnemyBoard(Game game, String playerId) {
        Board enemyBoard = gameService.getEnemyBoard(game.getId(), playerId);
        messagingTemplate.convertAndSend(
                "/topic/player/" + playerId + "/enemy-board",
                new EnemyBoardDto(enemyBoard)
        );
    }
}
