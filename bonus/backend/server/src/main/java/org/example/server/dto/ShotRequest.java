package org.example.server.dto;

public record ShotRequest(String gameId, String playerId, int x, int y) {}
