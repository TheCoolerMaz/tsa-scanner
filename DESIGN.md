# TSA Scanner — Game Design Document

## Concept

You are a TSA checkpoint scanner operator. Bags arrive on a conveyor belt, you x-ray them, and decide: **flag** or **pass**. Get it wrong and the consequences escalate.

Inspired by *Papers Please* — bureaucratic tension meets split-second judgment.

---

## Core Loop

1. **Bag arrives** on the conveyor belt (scrolls left to right)
2. Player activates **x-ray view** to see contents (toggle or hold)
3. Player inspects the x-ray silhouettes of items inside the bag
4. Player decides: **PASS** (green button) or **FLAG** (red button)
5. Result revealed — correct or incorrect
6. Score updated, next bag arrives
7. Shift ends after time runs out or quota is met

---

## Mechanics

### Conveyor Belt
- Bags scroll automatically at a set speed
- Speed increases with difficulty
- If a bag reaches the end without a decision, it counts as **passed** (auto-pass)
- Player can **pause the belt** briefly (limited uses per shift)

### X-Ray View
- Toggle between **normal view** (opaque bag) and **x-ray view** (translucent silhouettes)
- X-ray shows item outlines in classic blue/orange security scanner colors
- Some items overlap, making identification harder
- X-ray has a scan time — takes a moment to render (prevents instant decisions)

### Flagging
- **PASS** — let the bag through; correct if no weapons
- **FLAG** — pull the bag for inspection; correct if weapons present
- Wrong calls cost reputation/score
- Flagging a clean bag = **false positive** (annoys travelers, small penalty)
- Passing a weapon = **miss** (serious penalty, security breach)

---

## Item System

### Categories

| Category | Examples | X-Ray Appearance |
|----------|----------|-----------------|
| **Harmless** | Water bottle, laptop, shoes, book, umbrella | Distinct silhouettes |
| **Weapons** | Knife, gun, box cutter, brass knuckles | Recognizable weapon shapes |
| **Disguised Weapons** | Gun parts in a hair dryer, knife in an umbrella | Look like harmless items at first glance |
| **Suspicious Harmless** | Wrench, scissors (allowed), metal water bottle | Look weapon-like but are permitted |

### Difficulty Progression
- **Early shifts:** Obvious weapons (full gun outline), clear items
- **Mid shifts:** Disguised weapons appear, items overlap more
- **Late shifts:** Multiple suspicious items per bag, faster belt, ambiguous silhouettes

### Item Properties
- `name` — display name
- `category` — harmless / weapon / disguised_weapon / suspicious_harmless
- `silhouette` — x-ray texture region
- `difficulty` — when this item starts appearing (shift number)
- `confusableWith` — list of items this could be mistaken for

---

## Scoring

### Per Decision
- **Correct pass:** +10 points
- **Correct flag:** +25 points (catching weapons is harder)
- **False positive (flagging harmless):** -5 points
- **Miss (passing weapon):** -50 points

### Shift Rating
- Based on accuracy percentage and speed
- **S rank:** 95%+ accuracy, all bags processed
- **A rank:** 85%+ accuracy
- **B rank:** 70%+ accuracy
- **C rank:** Below 70%
- **FIRED:** Too many misses (3 weapons passed in one shift)

### Streak Bonus
- Consecutive correct decisions build a multiplier (x1.5, x2, x3)
- Broken on any wrong call

---

## Shift Progression

### Structure
- Game is divided into **shifts** (levels)
- Each shift has a **time limit** (e.g., 90 seconds) and a **minimum quota** (e.g., 15 bags)
- Must meet quota AND maintain minimum accuracy to advance
- Between shifts: stats screen, possibly upgrade selection

### Escalation (5 planned shifts)

| Shift | Belt Speed | New Items | Special |
|-------|-----------|-----------|---------|
| 1 | Slow | Basic harmless + obvious weapons | Tutorial hints |
| 2 | Medium | Scissors, tools (ambiguous) | False positives penalized more |
| 3 | Medium-Fast | Disguised weapons appear | Items can overlap |
| 4 | Fast | Full item set | Belt speed varies per bag |
| 5 | Very Fast | Everything + rare items | No hints, pure skill |

### Modifiers (stretch goal)
- **Rush hour:** More bags, faster belt
- **VIP bags:** Can't flag without certainty (higher miss penalty)
- **Random inspection:** Must flag a specific % regardless

---

## UI Layout

```
┌─────────────────────────────────────────────┐
│  SHIFT 1    SCORE: 1250    TIME: 01:23      │
│  ═══════════════════════════════════════════ │
│                                             │
│     ┌─────────────────────────────┐         │
│     │                             │         │
│     │      [BAG / X-RAY VIEW]     │         │
│     │                             │         │
│     └─────────────────────────────┘         │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ conveyor │
│                                             │
│  [X-RAY]          [✓ PASS]    [✗ FLAG]      │
│  Streak: x2       Bags: 8/15               │
└─────────────────────────────────────────────┘
```

### Controls
- **Space / X:** Toggle x-ray view
- **A / Left Arrow / Green button:** Pass
- **D / Right Arrow / Red button:** Flag
- **S / Down Arrow:** Pause belt (limited)
- **ESC:** Pause menu

---

## Art Direction

- **Pixel art** style, low resolution (320×180 scaled up)
- Color palette: Muted government blues and grays for the checkpoint
- X-ray view: Classic security scanner blue/orange color scheme
- Bags: Simple suitcase/backpack shapes with color variations
- Items: Clear silhouette designs that read well at small sizes

---

## Audio (stretch goal)

- Conveyor belt hum (ambient loop)
- Scanner beep on x-ray toggle
- Correct decision: satisfying ding
- Wrong decision: buzzer
- Weapon caught: alert siren
- Shift music: tension-building lo-fi or synth
