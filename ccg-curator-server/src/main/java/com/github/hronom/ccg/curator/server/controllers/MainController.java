package com.github.hronom.ccg.curator.server.controllers;

import com.github.hronom.ccg.curator.server.components.business.Player;
import com.github.hronom.ccg.curator.server.components.business.PlayersManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class MainController {
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private PlayersManager playersManager;

    @RequestMapping(value = "/", method = {RequestMethod.GET})
    @ApiOperation(value = "Check status.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful request."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 404, message = "Page not found."),
    })
    @ResponseBody
    public ResponseEntity<String> status() {
        return ResponseEntity.status(HttpStatus.OK).body("Good");
    }

    @RequestMapping(value = "/players", method = {RequestMethod.GET})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful request."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 404, message = "Page not found."),
    })
    @ResponseBody
    public ResponseEntity<Collection<Player>> getPlayers() {
        return ResponseEntity.status(HttpStatus.OK).body(playersManager.getPlayers());
    }
}
