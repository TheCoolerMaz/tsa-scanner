package com.tsascanner.game;

/**
 * Configuration for a single shift — defines what's forbidden,
 * belt speed, bags required, and briefing text.
 */
public class ShiftConfig {

    public final int shiftNumber;
    public final String[] forbiddenTags;
    public final String briefingTitle;
    public final String[] briefingLines;
    public final float beltSpeed;
    public final int bagsRequired;
    public final float shiftDuration; // seconds

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
    }

    /** Check if an item is forbidden in this shift. */
    public boolean isForbidden(Item item) {
        return item.hasAnyTag(forbiddenTags);
    }

    /** Get the shift config for a given shift number. */
    public static ShiftConfig getShift(int number) {
        switch (number) {
            case 1:
                return new ShiftConfig(1,
                    new String[]{"blade", "gun"},
                    "SHIFT 1 BRIEFING",
                    new String[]{
                        "Standard protocol.",
                        "No blades, no firearms.",
                        "",
                        "Forbidden: BLADE, GUN"
                    },
                    101f, 10, 90f
                );
            case 2:
                return new ShiftConfig(2,
                    new String[]{"blade", "gun", "liquid"},
                    "SHIFT 2 BRIEFING",
                    new String[]{
                        "New regulations.",
                        "Liquids are now restricted.",
                        "",
                        "Forbidden: BLADE, GUN, LIQUID"
                    },
                    127f, 12, 90f
                );
            case 3:
                return new ShiftConfig(3,
                    new String[]{"meat", "blade"},
                    "SHIFT 3 BRIEFING",
                    new String[]{
                        "Health advisory.",
                        "All raw meat is prohibited.",
                        "Blades still banned.",
                        "",
                        "Forbidden: MEAT, BLADE"
                    },
                    152f, 15, 90f
                );
            case 4:
                return new ShiftConfig(4,
                    new String[]{"electronic", "liquid", "gun"},
                    "SHIFT 4 BRIEFING",
                    new String[]{
                        "Security alert.",
                        "Electronics and liquids flagged.",
                        "Firearms obviously.",
                        "",
                        "Forbidden: ELECTRONIC, LIQUID, GUN"
                    },
                    178f, 18, 90f
                );
            case 5:
            default:
                return new ShiftConfig(Math.max(number, 5),
                    new String[]{"meat", "toy", "tool", "blade"},
                    "SHIFT 5 BRIEFING",
                    new String[]{
                        "New directive.",
                        "Toys, tools, meat, and blades.",
                        "Don't ask why.",
                        "",
                        "Forbidden: MEAT, TOY, TOOL, BLADE"
                    },
                    203f, 20, 90f
                );
        }
    }
}
