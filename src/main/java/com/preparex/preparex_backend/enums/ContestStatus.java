package com.preparex.preparex_backend.enums;

/**
 * Contest state machine.
 * Transitions: DRAFT→SCHEDULED→LIVE→ENDED→RESULTS_PUBLISHED
 * Special: ANY→CANCELLED
 */
public enum ContestStatus {
    DRAFT,
    SCHEDULED,
    LIVE,
    ENDED,
    RESULTS_PUBLISHED,
    CANCELLED
}
