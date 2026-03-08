package com.warroom.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

/**
 * Configuration for the AI War-Room Debate Workflow Engine.
 * 
 * Flow:
 * CREATED -> FILES_UPLOADED -> PROCESSING -> ANALYSIS_COMPLETE ->
 * RESULT_GENERATED
 * 
 * Failure:
 * Any state -> FAILED -> CREATED (on RESET)
 */
@Slf4j
@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<DebateStates, DebateEvents> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<DebateStates, DebateEvents> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<DebateStates, DebateEvents> states) throws Exception {
        states
                .withStates()
                .initial(DebateStates.CREATED)
                .states(EnumSet.allOf(DebateStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<DebateStates, DebateEvents> transitions) throws Exception {
        transitions
                .withExternal()
                .source(DebateStates.CREATED).target(DebateStates.FILES_UPLOADED)
                .event(DebateEvents.UPLOAD_FILES)
                .and()
                .withExternal()
                .source(DebateStates.FILES_UPLOADED).target(DebateStates.PROCESSING)
                .event(DebateEvents.START_PROCESSING)
                .and()
                .withExternal()
                .source(DebateStates.PROCESSING).target(DebateStates.ANALYSIS_COMPLETE)
                .event(DebateEvents.COMPLETE_ANALYSIS)
                .and()
                .withExternal()
                .source(DebateStates.ANALYSIS_COMPLETE).target(DebateStates.RESULT_GENERATED)
                .event(DebateEvents.GENERATE_RESULT)
                .and()
                .withExternal()
                .source(DebateStates.FILES_UPLOADED).target(DebateStates.FAILED)
                .event(DebateEvents.MARK_FAILED)
                .and()
                .withExternal()
                .source(DebateStates.PROCESSING).target(DebateStates.FAILED)
                .event(DebateEvents.MARK_FAILED)
                .and()
                .withExternal()
                .source(DebateStates.FAILED).target(DebateStates.CREATED)
                .event(DebateEvents.RESET);
    }

    /**
     * Listener for production logging of state transitions.
     */
    private StateMachineListener<DebateStates, DebateEvents> listener() {
        return new StateMachineListenerAdapter<DebateStates, DebateEvents>() {
            @Override
            public void stateChanged(State<DebateStates, DebateEvents> from, State<DebateStates, DebateEvents> to) {
                log.info("Debate state transition: {} -> {}",
                        from != null ? from.getId() : "NONE",
                        to != null ? to.getId() : "NONE");
            }

            @Override
            public void eventNotAccepted(org.springframework.messaging.Message<DebateEvents> event) {
                log.error("Debate event not accepted: {}", event.getPayload());
            }
        };
    }
}
