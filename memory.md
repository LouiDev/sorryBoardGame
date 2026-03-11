# memory.md — SorryBoardGame

Living project-state document. Updated after every successful change to the codebase.

---

## Project State

| Property           | Value                                              |
|--------------------|----------------------------------------------------|
| Status             | Playable prototype — turn flow + figure selection working |
| Commits            | 2 (initial commit + `feat(ui): implement ui logic`) |
| Last updated       | 2026-03-10                                         |
| Compiles           | **Yes**                                            |
| Runnable           | Yes                                                |

---

## Architecture

### Packages

| Package      | Status         | Notes                                                        |
|--------------|----------------|--------------------------------------------------------------|
| `models`     | Functional     | `Board`, `Team`, `BoardBuilder`, `GameFigure`, `GameState` done |
| `linkedlist`       | Complete       | All node types implemented and stable                        |
| `exceptions` | Complete       | `InvalidTreeStructureException` (1 class)                    |
| `ui`         | Functional     | Rendering + center info + highlight ring working; repaint connected |
| `listener`   | Functional     | SPACE → würfeln/bestätigen; ← → Figurauswahl; repaint() nach jeder Taste |

> **Architecture violation:** `linkedlist` imports `models.GameFigure` (`Node.java:3`,
> `ContentNode.java`). AGENTS.md states `linkedlist` must not import from `models`.

### Board Data Structure

Circular singly-linked list. Layout per team (×4):

```
TeamRootNode(team) → ContentNode×8 → GarageRootNode(nextTeam) → TeamRootNode(nextTeam) → ...
                                           └─ GarageNode×4 (side chain, finish lane)
```

The last `TeamRootNode` wraps back to `teams[0]`'s `TeamRootNode` — making the list circular.

### Node Hierarchy

```
Node (abstract)
├── ContentNode               — holds nullable GameFigure
│   └── TeamNode              — adds Team reference
│       ├── TeamRootNode      — team's start square; type marker
│       └── GarageRootNode    — owns a 4-node GarageNode side chain
│           (GarageNode extends TeamNode, not GarageRootNode)
└── EndNode                   — null-object sentinel; next() returns this
```

### Dependency Flow (intended vs. actual)

```
Intended:   ui → models → linkedlist/exceptions
                 listener → models

Actual violation:  linkedlist → models  (Node.java imports GameFigure)
```

---

## Implementation Status

| Feature                                        | Status                 |
|------------------------------------------------|------------------------|
| Circular linked list (all node types)          | Done                   |
| `BoardBuilder` fluent API                      | Done                   |
| `Team` model (id, color, name, home figures)   | Done — `name` field + accessor added |
| `GameFigure` model                             | Done — `team()` accessor added |
| `GameState` enum                               | Done (new file)        |
| `Board.startGame()`                            | Done                   |
| `Board.rollDice()`                             | Done                   |
| `Board.endTurn()`                              | Done                   |
| `Board.update()` — state machine               | Done                   |
| `Board.navigateSelection()`                    | Done                   |
| `Board.moveFigure(GameFigure, int)`            | Done (main ring traversal) |
| `Board.selectedFigure()` / `state()` / `lastRoll()` | Done              |
| Swing window (`BoardFrame`)                    | Done                   |
| Geometric board rendering (`BoardPanel`)       | Done                   |
| Finish-lane (garage) rendering                 | Done                   |
| Home-area rendering                            | Done — null guard added |
| Figure rendering with team color on board      | Done                   |
| Highlight-Ring für ausgewählte Figur           | Done                   |
| Center info panel (minimalist: Teamname in Farbe + Würfelzahl + Hinweis) | Done — box removed, name in team color |
| Pulsierender Highlight-Ring für ausgewählte Figur (Brett + Home)         | Done — Swing-Timer, Doppelring (Schatten + Weiß), animiert |
| Repaint after `board.update()`                 | Done                   |
| Keyboard input (SPACE + ←/→)                   | Done                   |
| `createBoard()` duplicate TeamRootNode fix     | Done                   |

---

## Critical Bugs

### 1. AGENTS.md documentation error — `EndNode` uses `super(true)` not `super(false)`
- **File:** `src/linkedlist/EndNode.java`, `AGENTS.md:197`
- **Detail:** AGENTS.md documents `super(false)`, but `EndNode` actually calls
  `super(true)`. The `skipInit` parameter name implies `true` = skip, so
  `true` is semantically correct. The docs contain a typo.
- **Fix needed:** Correct AGENTS.md line 197 to say `super(true)`.

---

## Missing Logic

| Feature                              | Notes                                                  |
|--------------------------------------|--------------------------------------------------------|
| Figure enters finish lane (garage)   | No transition from main track into `GarageNode` chain  |
| Send opponent home on landing        | No collision detection or "kick" logic                 |
| Win condition                        | No check for all 4 figures in the garage               |
| "Roll 6 with figure on board → roll again" | Not implemented                               |

---

## Model Gaps

| Gap                                         | File / Location                      |
|---------------------------------------------|--------------------------------------|
| `Team` has two redundant rootNode accessors  | `src/models/Team.java` — both `rootNode()` and `teamRootNode()` return the same field |
| `ContentNode` redundantly sets `next = new EndNode()` | `src/linkedlist/ContentNode.java` — `super()` already does this |
| `Node.getNodeForGameFigure()` returns `null` by default | Does not delegate to `next`; breaks traversal on mixed-type lists |
| AGENTS.md `ui/` description says "currently empty" | `ui/` now has 3 files; docs are stale |
| AGENTS.md `listener/` package undocumented  | Package added in second commit, not in Directory Structure section |
| Architecture violation: `linkedlist` imports `models` | `Node.java` imports `GameFigure`; reverses intended dependency direction |

---

## Next Steps (Prioritized)

1. **Implement "roll 6 with figure on board → roll again"** — currently not triggered after moving
2. **Implement finish-lane entry** — detect when a figure passes its own `GarageRootNode` and route into `GarageNode` chain
3. **Implement send-home collision** — when landing on occupied square belonging to another team
4. **Implement win condition** — all 4 figures of a team in garage = that team wins
5. **Fix architecture violation** — remove `linkedlist → models` import (move `GameFigure` or use generics)
6. **Fix AGENTS.md** — correct `super(false)` → `super(true)` typo; add `listener/` and actual `ui/` to directory structure
7. **Clean up redundant `rootNode()` / `teamRootNode()` in `Team`** — remove one of the duplicate accessors

---

## Change Log

| Date       | Change                                                                                        | Author  |
|------------|-----------------------------------------------------------------------------------------------|---------|
| 2026-03-10 | Initial commit — board data structure                                                         | human   |
| 2026-03-10 | Add Swing UI layer (`ui/`, `listener/`)                                                       | human   |
| 2026-03-10 | Create `memory.md` — initial analysis                                                         | agent   |
| 2026-03-10 | Implement turn/figure-selection state machine, moveFigure, center UI, highlight ring, repaint | agent   |
| 2026-03-10 | Fix off-by-one in `GarageNode.getAllRec()` — last node was never written to array, causing NPE on paint | agent   |
| 2026-03-10 | Reset `lastRoll` to 0 in `endTurn()` and after placing home figure, so dice display clears between turns | agent   |
| 2026-03-10 | Add `WAITING_FOR_SKIP_CONFIRM` state so skipped turns show the rolled number until player presses SPACE | agent   |
| 2026-03-10 | Remove premature `lastRoll = 0` after home-figure placement so the rolled 6 stays visible | agent   |
| 2026-03-10 | Minimalist center info: remove box, add readable team color name (Team Rot/Gelb/Blau/Grün) in team color; add `name` field to `Team` and `BoardBuilder` | agent   |
| 2026-03-10 | Animated pulsing highlight ring: Swing-Timer at 60fps, double-ring (dark shadow + white), now also covers home figures in `drawAllHomes` | agent   |
