package org.example.server.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.CreatePlayerRequest;
import org.example.server.dto.PlayerDto;
import org.example.server.entity.Player;
import org.example.server.service.GameService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RegisterController {
    private final GameService gameService;


    @PostMapping("/player")
    public PlayerDto createPlayer(@RequestBody CreatePlayerRequest request) {
        Player player = gameService.createPlayer(request.nickName());
        return new PlayerDto(player);
    }
}
