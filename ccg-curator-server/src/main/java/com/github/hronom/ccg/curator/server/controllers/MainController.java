package com.github.hronom.ccg.curator.server.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class MainController {
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    public MainController() {
    }

    @RequestMapping(value = "/", method = {RequestMethod.GET})
    @ApiOperation(value = "Check status.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful request."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 404, message = "Page not found."),
    })
    @ResponseBody
    public ResponseEntity<String> status() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello");
    }

    @RequestMapping(value = "/subscriptions", method = {RequestMethod.POST})
    @ApiOperation(value = "Add subscriptions with specified criterias.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful request."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 404, message = "Page not found."),
    })
    @ResponseBody
    public ResponseEntity<String> postSubscription(
        @RequestParam(value = "followUsersScreenNames", required = false) String[] followUsersScreenNames,
        @RequestParam(value = "mentionedUsersScreenNames", required = false) String[] mentionedUsersScreenNames,
        @RequestParam(value = "hashtags", required = false) String[] hashtags
    ) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello");
    }

    @RequestMapping(value = "/subscriptions/{subscription-id}", method = {RequestMethod.DELETE})
    @ApiOperation(value = "Delete subscriptions.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful request."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 404, message = "Page not found."),
    })
    @ResponseBody
    public ResponseEntity<String> deleteSubscription(@PathVariable("subscription-id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello");
    }

    @RequestMapping(value = "/subscriptions", method = {RequestMethod.DELETE})
    @ApiOperation(value = "Stop all subscriptions.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful request."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 404, message = "Page not found."),
    })
    @ResponseBody
    public ResponseEntity deleteSubscriptions() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello");
    }

    @RequestMapping(value = "/subscriptions", method = {RequestMethod.GET})
    @ApiOperation(value = "Get subscriptions.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful request."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 404, message = "Page not found."),
    })
    @ResponseBody
    public ResponseEntity<String> getSubscriptions() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello");
    }
}
