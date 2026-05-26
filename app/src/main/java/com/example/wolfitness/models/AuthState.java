package com.example.wolfitness.models;

/**
 * Class to represent the authentication state of the user
 */
public class AuthState {
    public static final int STATE_LOGGED_OUT = 0;
    public static final int STATE_LOGGED_IN = 1;
    public static final int STATE_PROFILE_INCOMPLETE = 2;
    public static final int STATE_GOAL_NOT_SELECTED = 3;

    private int state;
    private String userId;
    private String errorMessage;

    /**
     * Constructor with state only
     */
    public AuthState(int state) {
        this.state = state;
    }

    /**
     * Constructor with state and userId
     */
    public AuthState(int state, String userId) {
        this.state = state;
        this.userId = userId;
    }

    /**
     * Constructor with state, userId, and error message
     */
    public AuthState(int state, String userId, String errorMessage) {
        this.state = state;
        this.userId = userId;
        this.errorMessage = errorMessage;
    }

    /**
     * Check if user is logged in (any state except LOGGED_OUT)
     */
    public boolean isLoggedIn() {
        return state == STATE_LOGGED_IN ||
                state == STATE_PROFILE_INCOMPLETE ||
                state == STATE_GOAL_NOT_SELECTED;
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete() {
        return state == STATE_LOGGED_IN ||
                state == STATE_GOAL_NOT_SELECTED;
    }

    /**
     * Check if goal is selected
     */
    public boolean isGoalSelected() {
        return state == STATE_LOGGED_IN;
    }

    /**
     * Get the current state
     */
    public int getState() {
        return state;
    }

    /**
     * Set the current state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Get the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Convert state to string representation for logging
     */
    @Override
    public String toString() {
        switch (state) {
            case STATE_LOGGED_OUT:
                return "LOGGED_OUT";
            case STATE_LOGGED_IN:
                return "LOGGED_IN";
            case STATE_PROFILE_INCOMPLETE:
                return "PROFILE_INCOMPLETE";
            case STATE_GOAL_NOT_SELECTED:
                return "GOAL_NOT_SELECTED";
            default:
                return "UNKNOWN(" + state + ")";
        }
    }
}