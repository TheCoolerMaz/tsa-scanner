package com.tsascanner.game;

/**
 * Tracks all game state for a play session.
 * Scoring logic per DESIGN.md.
 */
public class GameState {

    // Scoring
    public int score;
    public int streak;
    public float multiplier;

    // Shift progression
    public int shiftNumber;
    public float shiftTimer;
    public float shiftDuration;
    public boolean shiftOver;

    // Quotas
    public int bagsProcessed;
    public int bagsRequired;

    // Strikes
    public int strikes;
    public static final int MAX_STRIKES = 3;

    // Accuracy
    public int correctDecisions;
    public int totalDecisions;

    // Last decision feedback
    public String feedbackText;
    public float feedbackTimer;
    public boolean feedbackCorrect;

    public GameState() {
        reset(1);
    }

    /** Reset for a new shift. */
    public void reset(int shift) {
        this.shiftNumber = shift;
        this.shiftTimer = 0;
        this.shiftDuration = 90f;
        this.shiftOver = false;

        this.bagsProcessed = 0;
        this.bagsRequired = getQuotaForShift(shift);

        this.strikes = 0;
        this.score = 0;
        this.streak = 0;
        this.multiplier = 1f;

        this.correctDecisions = 0;
        this.totalDecisions = 0;

        this.feedbackText = null;
        this.feedbackTimer = 0;
    }

    /** Continue to next shift, keeping score. */
    public void nextShift() {
        int prevScore = score;
        int prevCorrect = correctDecisions;
        int prevTotal = totalDecisions;

        reset(shiftNumber + 1);

        // Carry over cumulative stats
        this.score = prevScore;
        this.correctDecisions = prevCorrect;
        this.totalDecisions = prevTotal;
    }

    private int getQuotaForShift(int shift) {
        switch (shift) {
            case 1: return 10;
            case 2: return 12;
            case 3: return 15;
            case 4: return 18;
            default: return 20;
        }
    }

    /** Score a PASS decision. */
    public void scorePass(Bag bag) {
        totalDecisions++;

        if (bag.containsWeapon()) {
            // Missed a weapon! Bad!
            score -= 50;
            strikes++;
            streak = 0;
            multiplier = 1f;
            feedbackText = "MISSED WEAPON!";
            feedbackCorrect = false;
        } else {
            // Correct pass
            int points = (int) (10 * multiplier);
            score += points;
            streak++;
            updateMultiplier();
            feedbackText = "CORRECT!";
            feedbackCorrect = true;
            correctDecisions++;
        }

        bagsProcessed++;
        feedbackTimer = 1.5f;
        bag.state = Bag.BagState.PASSED;

        checkShiftEnd();
    }

    /** Score a FLAG decision. */
    public void scoreFlag(Bag bag) {
        totalDecisions++;

        if (bag.containsWeapon()) {
            // Correctly flagged!
            int points = (int) (25 * multiplier);
            score += points;
            streak++;
            updateMultiplier();
            feedbackText = "CORRECT!";
            feedbackCorrect = true;
            correctDecisions++;
        } else {
            // False positive
            score -= 5;
            streak = 0;
            multiplier = 1f;
            feedbackText = "FALSE FLAG!";
            feedbackCorrect = false;
        }

        bagsProcessed++;
        feedbackTimer = 1.5f;
        bag.state = Bag.BagState.FLAGGED;

        checkShiftEnd();
    }

    /** Auto-pass (timeout). */
    public void autoPass(Bag bag) {
        totalDecisions++;

        if (bag.containsWeapon()) {
            score -= 50;
            strikes++;
            streak = 0;
            multiplier = 1f;
            feedbackText = "MISSED WEAPON!";
            feedbackCorrect = false;
        } else {
            // Auto-pass of clean bag is "correct" but no bonus
            feedbackText = "AUTO-PASSED";
            feedbackCorrect = true;
            correctDecisions++;
        }

        bagsProcessed++;
        feedbackTimer = 1.5f;
        bag.state = Bag.BagState.PASSED;

        checkShiftEnd();
    }

    private void updateMultiplier() {
        if (streak >= 10) multiplier = 3f;
        else if (streak >= 5) multiplier = 2f;
        else if (streak >= 3) multiplier = 1.5f;
        else multiplier = 1f;
    }

    private void checkShiftEnd() {
        if (strikes >= MAX_STRIKES) {
            shiftOver = true;
        }
    }

    /** Get accuracy as percentage. */
    public float getAccuracy() {
        if (totalDecisions == 0) return 100f;
        return (correctDecisions / (float) totalDecisions) * 100f;
    }

    /** Get shift rating. */
    public String getRating() {
        if (strikes >= MAX_STRIKES) return "FIRED";
        float acc = getAccuracy();
        if (acc >= 95f && bagsProcessed >= bagsRequired) return "S";
        if (acc >= 85f) return "A";
        if (acc >= 70f) return "B";
        return "C";
    }

    /** Get time remaining as formatted string. */
    public String getTimeRemaining() {
        float remaining = Math.max(0, shiftDuration - shiftTimer);
        int minutes = (int) (remaining / 60);
        int seconds = (int) (remaining % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    /** Belt speed multiplier based on shift. */
    public float getBeltSpeed() {
        switch (shiftNumber) {
            case 1: return 60f;
            case 2: return 75f;
            case 3: return 90f;
            case 4: return 105f;
            default: return 120f;
        }
    }
}
