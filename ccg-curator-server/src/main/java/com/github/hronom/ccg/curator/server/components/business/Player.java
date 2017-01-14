package com.github.hronom.ccg.curator.server.components.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Player {
    private static final Logger logger = LogManager.getLogger();

    private final long id;
    private final String name;

    public Player(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
