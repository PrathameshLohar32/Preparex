package com.preparex.preparex_backend.event;

import com.preparex.preparex_backend.entity.Submission;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published after a submission is saved.
 * Consumed asynchronously by listeners for streak updates and analytics.
 */
@Getter
public class SubmissionSavedEvent extends ApplicationEvent {

    private final Submission submission;

    public SubmissionSavedEvent(Object source, Submission submission) {
        super(source);
        this.submission = submission;
    }
}
