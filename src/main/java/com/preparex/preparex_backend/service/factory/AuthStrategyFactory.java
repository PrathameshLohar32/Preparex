package com.preparex.preparex_backend.service.factory;

import com.preparex.preparex_backend.enums.AuthType;
import com.preparex.preparex_backend.exception.InvalidCredentialsException;
import com.preparex.preparex_backend.service.strategy.AuthStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that resolves the correct AuthStrategy for a given AuthType.
 *
 * All AuthStrategy beans are auto-injected as a list and indexed by their supported type.
 * To add a new strategy, simply implement AuthStrategy and annotate it with @Component —
 * no changes to this factory are needed.
 */
@Slf4j
@Component
public class AuthStrategyFactory {

    private final Map<AuthType, AuthStrategy> strategyMap;

    public AuthStrategyFactory(java.util.List<AuthStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AuthStrategy::getSupportedAuthType, Function.identity()));
        log.info("Registered auth strategies: {}", strategyMap.keySet());
    }

    /**
     * Returns the strategy for the given AuthType.
     *
     * @throws InvalidCredentialsException if no strategy is registered for the type
     */
    public AuthStrategy resolve(AuthType authType) {
        AuthStrategy strategy = strategyMap.get(authType);
        if (strategy == null) {
            log.warn("No strategy found for authType={}", authType);
            throw new InvalidCredentialsException();
        }
        return strategy;
    }
}
