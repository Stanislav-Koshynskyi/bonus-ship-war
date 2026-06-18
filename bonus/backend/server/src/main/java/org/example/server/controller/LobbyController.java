package org.example.server.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.GameDto;
import org.example.server.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LobbyController {
    private final GameService gameService;

    @GetMapping("/game/waiting")
    public List<GameDto> getWaitingGames() {
        return gameService.getWaitingGames();
    }
}
