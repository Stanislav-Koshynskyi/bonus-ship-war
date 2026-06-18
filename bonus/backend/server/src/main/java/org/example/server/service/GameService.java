package org.example.server.service;

import org.example.server.dto.GameDto;
import org.example.server.entity.*;
import org.example.server.entity.enums.CellState;
import org.example.server.entity.enums.GameState;
import org.example.server.entity.enums.PlayerState;
import org.example.server.entity.enums.ShipOrientation;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.server.entity.Board.MAX_SHIP_COUNT_PER_LENGTH;
import static org.example.server.entity.enums.ShotResultType.*;

@Service
public class GameService {
    private ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();

    public Game createGame(String creatorId) {
        Player creator = players.get(creatorId);
        if (creator == null) throw new IllegalArgumentException("not found player with id " + creatorId);
        Game game = new Game(creator);
        creator.setState(PlayerState.IN_LOBBY);
        games.put(game.getId(), game);
        return game;
    }

    public Game joinGame(String gameId, String  joinerId) {
        Player joiner = players.get(joinerId);
        if (joiner == null) throw new IllegalArgumentException("not found player with id " + joinerId);
        Game game = games.get(gameId);
        if (game == null) throw new IllegalArgumentException("Game with id " + gameId + " not found");
        if (game.getState() != GameState.WAITING_FOR_OPPONENT) throw new IllegalArgumentException("Game has started");
        synchronized (game) {
            game.join(joiner);
            joiner.setState(PlayerState.IN_LOBBY);
            generateBoard(gameId, game.getPlayer1().getId());
            generateBoard(gameId, game.getPlayer2().getId());
            return game;
        }
    }

    public Game ready(String gameId, String playerId) {
        Player player = players.get(playerId);
        if (player == null) throw new IllegalArgumentException("not found player with id " + playerId);
        Game game = games.get(gameId);
        if (game == null) throw new IllegalArgumentException("Game with id " + gameId + " not found");
        if (game.getState() != GameState.SHIP_PLACEMENT) throw new IllegalArgumentException("Game has started");
        synchronized (game) {
            if (!game.playerInGame(player)) throw new IllegalArgumentException("That not your game");
            if (game.playerHasBoard(player)) {
                player.setState(PlayerState.READY);
            } else {
                throw new IllegalArgumentException("Player have not board");
            }
            if (game.everyoneReady()) {
                game.setState(GameState.IN_PROGRESS);
                game.getPlayer1().setState(PlayerState.IN_GAME);
                game.getPlayer2().setState(PlayerState.IN_GAME);
            }
            return game;
        }
    }

    public ShotResult processShot(String gameId, String attackerId, int x, int y) {
        Player attacker = players.get(attackerId);
        if (attacker == null) throw new IllegalArgumentException("not found player with id " + attackerId);
        Game game = games.get(gameId);
        if (game == null) throw new IllegalArgumentException("Game with id " + gameId + " not found");
        if (game.getState() != GameState.IN_PROGRESS) throw new IllegalArgumentException("Game has not started");
        if (!game.playerInGame(attacker)) throw new IllegalArgumentException("That not your game");
        if (!game.getWhoTurnNow().equals(attacker)) throw new IllegalArgumentException("Not not your turn");
        synchronized (game) {
            Board opponentBoard = game.getOpponentBoard(attacker);
            ShotResult shotResult = new ShotResult();
            if (opponentBoard.getCellState(x, y) != CellState.VOID
                    && opponentBoard.getCellState(x, y) != CellState.SHIP)
                return new ShotResult(List.of(), ERROR);
            if (opponentBoard.getCellState(x, y) == CellState.SHIP) {
                Ship ship = opponentBoard.getShipAt(x, y);
                if (ship.applyDamage()) {
                    List<Cell> changedCells = new ArrayList<>();
                    for (Point point : ship.getCoord()) {
                        opponentBoard.setCellState(point.x, point.y, CellState.DEAD_SHIP);
                        changedCells.add(new Cell(point.x, point.y, CellState.DEAD_SHIP));
                    }
                    for (Point point : ship.getCoord()) {
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                int nx = point.x + dx;
                                int ny = point.y + dy;
                                if (nx >= 0 && nx < Board.BOARD_SIZE && ny >= 0 && ny < Board.BOARD_SIZE) {
                                    if (opponentBoard.getCellState(nx, ny) == CellState.VOID) {
                                        opponentBoard.setCellState(nx, ny, CellState.MISS);
                                        changedCells.add(new Cell(nx, ny, CellState.MISS));
                                    }
                                }
                            }
                        }
                    }
                    shotResult.setChangedCells(changedCells);

                    if (opponentBoard.defeat()) {
                        game.setWinner(attacker);
                        game.setState(GameState.FINISHED);
                        shotResult.setShotResultType(WIN);
                        shotResult.setWinner(attacker);
                        games.remove(gameId);
                    } else {
                        shotResult.setShotResultType(KILL);

                    }
                } else {
                    opponentBoard.setCellState(x, y, CellState.ATTACKED_SHIP_PART);
                    shotResult = new ShotResult(List.of(new Cell(
                            x, y, CellState.ATTACKED_SHIP_PART
                    )), DAMAGE);
                }
            } else {
                opponentBoard.setCellState(x, y, CellState.MISS);
                shotResult = new ShotResult(List.of(new Cell(
                        x, y, CellState.MISS
                )), MISS);
                game.passTurn();
            }
            return shotResult;
        }
    }

    public Board generateBoard(String gameId, String playerId) {
        Player player = players.get(playerId);
        if (player == null) throw new IllegalArgumentException("not found player with id " + playerId);
        if (player.getState() != PlayerState.IN_LOBBY) throw new IllegalArgumentException("Incorrect phase for placing ship");
        Game game = games.get(gameId);
        if (game == null) throw new IllegalArgumentException("Game with id " + gameId + " not found");
        if (game.getState() != GameState.SHIP_PLACEMENT)
            throw new IllegalArgumentException("Not a ship placement phase");
        if (!game.playerInGame(player)) throw new IllegalArgumentException("That not your game");
        synchronized (game) {
            List<Ship> ships = generateShips();
            Board board = new Board(ships);
            game.setPlayerBoard(player, board);
            return board;
        }
    }

    private List<Ship> generateShips() {
        List<Ship> ships = new ArrayList<>();
        boolean[][] occupied = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];

        for (int size = MAX_SHIP_COUNT_PER_LENGTH.length; size >= 1; size--) {
            int count = MAX_SHIP_COUNT_PER_LENGTH[size - 1];
            for (int i = 0; i < count; i++) {
                Ship ship = placeShip(size, occupied);
                ships.add(ship);
                occupyWithBuffer(ship, occupied);
            }
        }
        return ships;
    }

    private Ship placeShip(int size, boolean[][] occupied) {
        for (int attempt = 0; attempt < 1000; attempt++) {
            ShipOrientation orientation = ThreadLocalRandom.current().nextBoolean()
                    ? ShipOrientation.HORIZONTAL : ShipOrientation.VERTICAL;
            int maxX = orientation == ShipOrientation.HORIZONTAL ? Board.BOARD_SIZE - size : Board.BOARD_SIZE - 1;
            int maxY = orientation == ShipOrientation.VERTICAL ? Board.BOARD_SIZE - size : Board.BOARD_SIZE - 1;

            Ship candidate = new Ship(size,
                    ThreadLocalRandom.current().nextInt(maxX + 1),
                    ThreadLocalRandom.current().nextInt(maxY + 1),
                    orientation);

            if (isFree(candidate, occupied)) return candidate;
        }
        throw new IllegalStateException("Placing error");
    }

    private boolean isFree(Ship ship, boolean[][] occupied) {
        for (Point p : ship.getCoord()) {
            if (occupied[p.x][p.y]) return false;
        }
        return true;
    }

    private void occupyWithBuffer(Ship ship, boolean[][] occupied) {
        for (Point point : ship.getCoord()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = point.x + dx, ny = point.y + dy;
                    if (nx >= 0 && nx < Board.BOARD_SIZE && ny >= 0 && ny < Board.BOARD_SIZE) {
                        occupied[nx][ny] = true;
                    }
                }
            }
        }
    }
    public Player createPlayer(String nickname){
        Player player = new Player(nickname);
        players.put(player.getId(), player);
        return player;
    }

    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    public Board getPlayerBoard(String gameId, String playerId) {
        Game game = games.get(gameId);
        if (game == null) return null;
        Player player = players.get(playerId);
        if (player == null) return null;
        return game.getPlayerBoard(player);
    }

    public Board getEnemyBoard(String gameId, String playerId) {
        Game game = games.get(gameId);
        if (game == null) return null;
        Player player = players.get(playerId);
        if (player == null) return null;
        return game.getOpponentBoard(player);
    }

    public List<GameDto> getWaitingGames() {
        return games.values().stream()
                .filter(g -> g.getState() == GameState.WAITING_FOR_OPPONENT)
                .map(GameDto::new)
                .toList();
    }
}
