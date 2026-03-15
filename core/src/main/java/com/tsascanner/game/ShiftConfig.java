package com.tsascanner.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration for a single shift — defines what's forbidden,
 * belt speed, bags required, and briefing text.
 */
public class ShiftConfig {

    public static class ForbiddenRule {
        public final String[] forbiddenTags;
        public final Bag.BagColor requiredColor; // null = any
        public final Boolean oddOnly;             // null = any, true = odd, false = even
        public final String description;

        public ForbiddenRule(String[] forbiddenTags, Bag.BagColor requiredColor,
                            Boolean oddOnly, String description) {
            this.forbiddenTags = forbiddenTags;
            this.requiredColor = requiredColor;
            this.oddOnly = oddOnly;
            this.description = description;
        }

        public boolean appliesTo(Bag bag) {
            if (requiredColor != null && bag.color != requiredColor) return false;
            if (oddOnly != null) {
                boolean isOdd = (bag.bagNumber % 2 != 0);
                if (oddOnly != isOdd) return false;
            }
            return true;
        }
    }

    public final int shiftNumber;
    public final String[] forbiddenTags; // kept for backward compat — union of all rules
    public final String briefingTitle;
    public final String[] briefingLines;
    public final float beltSpeed;
    public final int bagsRequired;
    public final float shiftDuration; // seconds
    public final List<ForbiddenRule> rules;

    public ShiftConfig(int shiftNumber, String[] forbiddenTags,
                       String briefingTitle, String[] briefingLines,
                       float beltSpeed, int bagsRequired, float shiftDuration) {
        this.shiftNumber = shiftNumber;
        this.forbiddenTags = forbiddenTags;
        this.briefingTitle = briefingTitle;
        this.briefingLines = briefingLines;
        this.beltSpeed = beltSpeed;
        this.bagsRequired = bagsRequired;
        this.shiftDuration = shiftDuration;
        // Wrap into a single catch-all rule
        this.rules = new ArrayList<>();
        this.rules.add(new ForbiddenRule(forbiddenTags, null, null, tagsToDescription(forbiddenTags)));
    }

    public ShiftConfig(int shiftNumber, List<ForbiddenRule> rules,
                       String briefingTitle, String[] briefingLines,
                       float beltSpeed, int bagsRequired, float shiftDuration) {
        this.shiftNumber = shiftNumber;
        this.rules = rules;
        this.briefingTitle = briefingTitle;
        this.briefingLines = briefingLines;
        this.beltSpeed = beltSpeed;
        this.bagsRequired = bagsRequired;
        this.shiftDuration = shiftDuration;
        this.forbiddenTags = getAllForbiddenTags();
    }

    private static String tagsToDescription(String[] tags) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(tags[i].toUpperCase());
        }
        sb.append(" forbidden");
        return sb.toString();
    }

    /** Check if an item is forbidden for a specific bag (context-aware). */
    public boolean isForbidden(Item item, Bag bag) {
        for (ForbiddenRule rule : rules) {
            if (rule.appliesTo(bag)) {
                if (rule.forbiddenTags.length == 0) {
                    // Empty tags = everything allowed (VIP)
                    return false;
                }
                return item.hasAnyTag(rule.forbiddenTags);
            }
        }
        // No rule matches this bag — item is not forbidden
        return false;
    }

    /** Check if an item is forbidden (backward compat — checks against ALL rules' tags). */
    public boolean isForbidden(Item item) {
        return item.hasAnyTag(forbiddenTags);
    }

    /** Get union of all rules' forbidden tags. */
    public String[] getAllForbiddenTags() {
        Set<String> allTags = new LinkedHashSet<>();
        for (ForbiddenRule rule : rules) {
            allTags.addAll(Arrays.asList(rule.forbiddenTags));
        }
        return allTags.toArray(new String[0]);
    }

    /** Get the shift config for a given shift number. */
    public static ShiftConfig getShift(int number) {
        switch (number) {
            case 1: {
                List<ForbiddenRule> rules = new ArrayList<>();
                rules.add(new ForbiddenRule(
                    new String[]{"blade", "gun"}, null, null,
                    "All bags: BLADE, GUN forbidden"
                ));
                return new ShiftConfig(1, rules,
                    "SHIFT 1 BRIEFING",
                    new String[]{
                        "Standard protocol.",
                        "No blades, no firearms.",
                        "",
                        "Brown bags only today."
                    },
                    101f, 10, 90f
                );
            }
            case 2: {
                List<ForbiddenRule> rules = new ArrayList<>();
                rules.add(new ForbiddenRule(
                    new String[]{"blade", "gun", "liquid"}, null, null,
                    "All bags: BLADE, GUN, LIQUID forbidden"
                ));
                return new ShiftConfig(2, rules,
                    "SHIFT 2 BRIEFING",
                    new String[]{
                        "New regulations.",
                        "Liquids are now restricted.",
                        "",
                        "Brown bags only."
                    },
                    127f, 12, 90f
                );
            }
            case 3: {
                List<ForbiddenRule> rules = new ArrayList<>();
                rules.add(new ForbiddenRule(
                    new String[]{}, Bag.BagColor.RED, null,
                    "VIP — Everything allowed"
                ));
                rules.add(new ForbiddenRule(
                    new String[]{"blade", "gun", "liquid"}, Bag.BagColor.BLUE, null,
                    "BLADE, GUN, LIQUID forbidden"
                ));
                rules.add(new ForbiddenRule(
                    new String[]{"food", "blade"}, Bag.BagColor.GREEN, null,
                    "FOOD, BLADE forbidden"
                ));
                return new ShiftConfig(3, rules,
                    "SHIFT 3 BRIEFING",
                    new String[]{
                        "Color-coded bags in effect.",
                        "Red bags are VIP — pass everything.",
                        "Rules vary by bag color.",
                        "",
                        "Check the reference card."
                    },
                    152f, 15, 90f
                );
            }
            case 4: {
                List<ForbiddenRule> rules = new ArrayList<>();
                rules.add(new ForbiddenRule(
                    new String[]{}, Bag.BagColor.RED, null,
                    "VIP — Everything allowed"
                ));
                rules.add(new ForbiddenRule(
                    new String[]{"electronic", "liquid"}, null, true,
                    "Odd-numbered: ELECTRONIC, LIQUID forbidden"
                ));
                rules.add(new ForbiddenRule(
                    new String[]{"blade", "tool"}, null, false,
                    "Even-numbered: BLADE, TOOL forbidden"
                ));
                return new ShiftConfig(4, rules,
                    "SHIFT 4 BRIEFING",
                    new String[]{
                        "Numbered bag system active.",
                        "Red bags are VIP.",
                        "Odd bags: no electronics or liquids.",
                        "Even bags: no blades or tools.",
                        "",
                        "Check bag numbers carefully."
                    },
                    178f, 18, 90f
                );
            }
            case 5:
            default: {
                List<ForbiddenRule> rules = new ArrayList<>();
                rules.add(new ForbiddenRule(
                    new String[]{"meat"}, Bag.BagColor.RED, true,
                    "MEAT forbidden"
                ));
                rules.add(new ForbiddenRule(
                    new String[]{"toy", "tool"}, Bag.BagColor.BLUE, false,
                    "TOY, TOOL forbidden"
                ));
                rules.add(new ForbiddenRule(
                    new String[]{"liquid", "electronic"}, Bag.BagColor.GREEN, null,
                    "LIQUID, ELECTRONIC forbidden"
                ));
                rules.add(new ForbiddenRule(
                    new String[]{"blade", "gun"}, Bag.BagColor.BROWN, null,
                    "BLADE, GUN forbidden"
                ));
                return new ShiftConfig(Math.max(number, 5), rules,
                    "SHIFT 5 BRIEFING",
                    new String[]{
                        "Full protocol.",
                        "Colors AND numbers matter.",
                        "Each combination has different rules.",
                        "",
                        "Study the reference card."
                    },
                    203f, 20, 90f
                );
            }
        }
    }
}
