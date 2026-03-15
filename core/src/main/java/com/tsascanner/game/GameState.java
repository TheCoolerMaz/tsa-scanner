package com.tsascanner.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks all game state for a play session.
 * Handles belt scoring, inspection queue, and per-item inspection scoring.
 */
public class GameState {

    // Scoring
    public int score;
    public int streak;
    public float multiplier;

    // Shift config
    public ShiftConfig shiftConfig;

    // Shift progression
    public int shiftNumber;
    public float shiftTimer;
    public boolean shiftOver;

    // Quotas
    public int bagsProcessed;  // total bags that passed or were inspected

    // Strikes
    public int strikes;
    public static final int MAX_STRIKES = 3;

    // Accuracy — now tracks item-level classifications
    public int correctClassifications;
    public int totalClassifications;
    public int itemsClassifiedCorrectly;
    public int itemsClassifiedIncorrectly;

    // Bags pulled / passed
    public int bagsPassed;
    public int bagsInspected;

    // Inspection queue
    public List<Bag> inspectionQueue;
    public static final int MAX_QUEUE_SIZE = 4;
    public Bag currentInspectionBag;
    public int selectedItemIndex;

    // Last decision feedback
    public String feedbackText;
    public float feedbackTimer;
    public boolean feedbackCorrect;

    // Briefing state
    public boolean showBriefing;

    public GameState() {
        inspectionQueue = new ArrayList<>();
        reset(1);
    }

    /** Reset for a new shift. */
    public void reset(int shift) {
        this.shiftNumber = shift;
        this.shiftConfig = ShiftConfig.getShift(shift);
        this.shiftTimer = 0;
        this.shiftOver = false;

        this.bagsProcessed = 0;
        this.bagsPassed = 0;
        this.bagsInspected = 0;

        this.strikes = 0;
        this.score = 0;
        this.streak = 0;
        this.multiplier = 1f;

        this.correctClassifications = 0;
        this.totalClassifications = 0;
        this.itemsClassifiedCorrectly = 0;
        this.itemsClassifiedIncorrectly = 0;

        this.feedbackText = null;
        this.feedbackTimer = 0;

        this.inspectionQueue.clear();
        this.currentInspectionBag = null;
        this.selectedItemIndex = -1;

        this.showBriefing = true;
    }

    /** Continue to next shift. */
    public void nextShift() {
        reset(shiftNumber + 1);
    }

    // ==================== BELT SCORING ====================

    /** A bag auto-passed off the belt (player didn't pull it). */
    public void autoPassed(Bag bag) {
        if (bag.containsForbidden(shiftConfig)) {
            // Missed a forbidden bag! Strike!
            strikes++;
            streak = 0;
            multiplier = 1f;
            feedbackText = "MISSED THREAT!";
            feedbackCorrect = false;
            feedbackTimer = 1.5f;
        }
        // Clean bags passing is fine — no action needed
        bag.state = Bag.BagState.PASSED;
        bagsPassed++;
        bagsProcessed++;
        checkShiftEnd();
    }

    /** Player pulls a bag to the inspection queue. */
    public boolean pullBag(Bag bag) {
        if (inspectionQueue.size() >= MAX_QUEUE_SIZE) {
            // Queue overflow — auto-fail the oldest bag
            Bag overflow = inspectionQueue.remove(0);
            failBag(overflow);
        }

        bag.state = Bag.BagState.PULLED;
        inspectionQueue.add(bag);

        // If nothing is currently being inspected, load this one
        if (currentInspectionBag == null) {
            loadNextInspectionBag();
        }
        return true;
    }

    /** Load the next bag from the queue for inspection. */
    public void loadNextInspectionBag() {
        if (inspectionQueue.isEmpty()) {
            currentInspectionBag = null;
            selectedItemIndex = -1;
            return;
        }
        currentInspectionBag = inspectionQueue.remove(0);
        currentInspectionBag.state = Bag.BagState.INSPECTING;
        selectedItemIndex = -1;

        // Reset all item marks
        for (Item item : currentInspectionBag.contents) {
            item.mark = Item.InspectionMark.UNMARKED;
        }
    }

    // ==================== INSPECTION SCORING ====================

    /** Mark the selected item as CLEAR. */
    public void markSelectedClear() {
        if (currentInspectionBag == null || selectedItemIndex < 0
            || selectedItemIndex >= currentInspectionBag.contents.size()) return;

        Item item = currentInspectionBag.contents.get(selectedItemIndex);
        item.mark = Item.InspectionMark.MARKED_CLEAR;
    }

    /** Mark the selected item as FORBIDDEN. */
    public void markSelectedForbidden() {
        if (currentInspectionBag == null || selectedItemIndex < 0
            || selectedItemIndex >= currentInspectionBag.contents.size()) return;

        Item item = currentInspectionBag.contents.get(selectedItemIndex);
        item.mark = Item.InspectionMark.MARKED_FORBIDDEN;
    }

    /** Submit the inspected bag. Scores each item. Returns true if all correct. */
    public boolean submitInspection() {
        if (currentInspectionBag == null) return false;

        int correct = 0;
        int wrong = 0;
        int missedThreats = 0;
        int total = currentInspectionBag.contents.size();

        for (Item item : currentInspectionBag.contents) {
            boolean actuallyForbidden = shiftConfig.isForbidden(item);
            boolean markedForbidden = item.mark == Item.InspectionMark.MARKED_FORBIDDEN;
            boolean markedClear = item.mark == Item.InspectionMark.MARKED_CLEAR;

            totalClassifications++;

            if (markedForbidden && actuallyForbidden) {
                // Correctly identified forbidden item
                correct++;
                correctClassifications++;
                itemsClassifiedCorrectly++;
            } else if (markedClear && !actuallyForbidden) {
                // Correctly identified clean item
                correct++;
                correctClassifications++;
                itemsClassifiedCorrectly++;
            } else if (item.mark == Item.InspectionMark.UNMARKED) {
                // Unmarked items default to CLEAR
                if (!actuallyForbidden) {
                    correct++;
                    correctClassifications++;
                    itemsClassifiedCorrectly++;
                } else {
                    // Missed a forbidden item — strike
                    wrong++;
                    missedThreats++;
                    itemsClassifiedIncorrectly++;
                }
            } else {
                // Wrong classification
                wrong++;
                itemsClassifiedIncorrectly++;
                // Marked clear but actually forbidden = missed threat
                if (markedClear && actuallyForbidden) {
                    missedThreats++;
                }
            }
        }

        // Missed threats in inspection count as strikes
        if (missedThreats > 0) {
            strikes += missedThreats;
        }

        boolean allCorrect = (correct == total);

        if (allCorrect && total > 0) {
            streak++;
            updateMultiplier();
            feedbackText = "PERFECT!";
            feedbackCorrect = true;
        } else if (wrong > 0) {
            streak = 0;
            multiplier = 1f;
            feedbackText = correct + "/" + total + " CORRECT";
            feedbackCorrect = false;
        } else {
            feedbackText = "BAG CLEARED";
            feedbackCorrect = true;
        }

        feedbackTimer = 1.5f;
        currentInspectionBag.state = Bag.BagState.FLAGGED;
        bagsInspected++;
        bagsProcessed++;

        // Load next bag from queue
        currentInspectionBag = null;
        selectedItemIndex = -1;
        loadNextInspectionBag();

        checkShiftEnd();
        return allCorrect;
    }

    /** Auto-fail a bag from queue overflow. */
    private void failBag(Bag bag) {
        strikes++;
        feedbackText = "QUEUE OVERFLOW!";
        feedbackCorrect = false;
        feedbackTimer = 1.5f;
        bag.state = Bag.BagState.FLAGGED;
        bagsProcessed++;
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
        if (totalClassifications == 0) return 100f;
        return (correctClassifications / (float) totalClassifications) * 100f;
    }

    /** Get shift rating. */
    public String getRating() {
        if (strikes >= MAX_STRIKES) return "FIRED";
        float acc = getAccuracy();
        if (acc >= 95f && bagsProcessed >= shiftConfig.bagsRequired) return "S";
        if (acc >= 85f) return "A";
        if (acc >= 70f) return "B";
        return "C";
    }

    /** Get time remaining as formatted string. */
    public String getTimeRemaining() {
        float remaining = Math.max(0, shiftConfig.shiftDuration - shiftTimer);
        int minutes = (int) (remaining / 60);
        int seconds = (int) (remaining % 60);
        return String.format("%d:%02d", minutes, seconds);
    }
}
