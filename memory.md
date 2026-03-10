# memory.md — SorryBoardGame

Living project-state document. Updated after every successful change to the codebase.

---

## Project State

| Property           | Value                                              |
|--------------------|----------------------------------------------------|
| Status             | Early prototype — data structure + rendering done, game logic absent |
| Commits            | 2 (initial commit + `feat(ui): implement ui logic`) |
| Last updated       | 2026-03-10                                         |
| Compiles           | **No** — `Board.java:48` has a compile error (see Critical Bugs) |
| Runnable           | No                                                 |

---

## Architecture

### Packages

| Package      | Status         | Notes                                                        |
|--------------|----------------|--------------------------------------------------------------|
| `models`     | Partial        | `Board`, `Team`, `BoardBuilder` functional; `GameFigure` is a skeleton |
| `tree`       | Complete       | All node types implemented and stable                        |
| `exceptions` | Complete       | `InvalidTreeStructureException` (1 class)                    |
| `ui`         | Partial        | Rendering works; no repaint trigger; figures drawn in wrong color |
| `listener`   | Skeleton       | `InputListener` wires SPACE → `board.update()`, but `update()` is broken |

> **Architecture violation:** `tree` imports `models.GameFigure` (`Node.java:3`,
> `ContentNode.java`). AGENTS.md states `tree` must not import from `models`.

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
Intended:   ui → models → tree/exceptions
                 listener → models

Actual violation:  tree → models  (Node.java imports GameFigure)
```

---

## Implementation Status

| Feature                                        | Status                 |
|------------------------------------------------|------------------------|
| Circular linked list (all node types)          | Done                   |
| `BoardBuilder` fluent API                      | Done                   |
| `Team` model (id, color, home figures)         | Done                   |
| `GameFigure` model                             | Skeleton — no accessor |
| `Board.startGame()`                            | Minimal (sets index=0) |
| `Board.rollDice()`                             | Done (`Random.nextInt(1,7)`) |
| `Board.endTurn()`                              | Done                   |
| `Board.update()` — turn logic                  | Broken (compile error) |
| `Board.moveFigure(GameFigure)`                 | Empty stub             |
| Swing window (`BoardFrame`)                    | Done                   |
| Geometric board rendering (`BoardPanel`)       | Done                   |
| Finish-lane (garage) rendering                 | Done                   |
| Home-area rendering                            | Done (always draws all 4 — bug) |
| Figure rendering with team color on board      | Not done (draws dark gray) |
| Repaint after `board.update()`                 | Not done               |
| Keyboard input (SPACE)                         | Done (but target broken) |

---

## Critical Bugs

### 1. Compile error — `Board.update()` calls `moveFigure()` with no arguments
- **File:** `src/models/Board.java:48`
- **Detail:** `moveFigure()` is called with zero arguments, but the method
  signature is `private void moveFigure(GameFigure figure)`. This prevents
  the entire project from compiling.
- **Fix needed:** Determine which `GameFigure` to pass (the figure just placed
  on the board, or a figure already on the track) and pass it to the call.

### 2. Duplicate `TeamRootNode` for `teams[0]` in `createBoard()`
- **File:** `src/models/Board.java:76–98`
- **Detail:** A `TeamRootNode` for `teams[0]` is created before the loop (`n`)
  and stored as `root`. Inside the loop, when `i == teams.length - 1`,
  `nextTeam = teams[0]` and a *second* `TeamRootNode` is created and wired to
  `teams[0].teamRootNode(trn)`. As a result, `board.root()` and
  `teams[0].teamRootNode()` point to **different objects**. Any logic
  navigating from `team.teamRootNode()` and comparing to `board.root()` will
  fail to detect the start of the board.
- **Fix needed:** Associate the first `TeamRootNode n` with `teams[0]` (call
  `teams[0].teamRootNode(n)`) before the loop, and skip creating a new
  `TeamRootNode` for `teams[0]` at the end of the loop.

### 3. UI never repaints after `board.update()`
- **File:** `src/listener/InputListener.java`, `src/ui/BoardFrame.java`
- **Detail:** `InputListener` holds a `Board` reference and calls
  `board.update()` on SPACE, but there is no call to `repaint()` or any
  observer mechanism. The board state changes but the screen never refreshes.
- **Fix needed:** Pass a `BoardPanel` (or `Runnable repaint`) reference to
  `InputListener`, or add a model-listener/observer pattern.

### 4. `drawAllHomes` always draws all 4 home figures regardless of `null`
- **File:** `src/ui/BoardPanel.java:228–235`
- **Detail:** The loop iterates `home[]` but always calls `drawFigure()` without
  checking `if (home[j] != null)`. Once figures are placed on the board and
  their `home[]` slot is set to `null`, they will still be drawn in the home
  area.
- **Fix needed:** Add `if (home[j] != null)` guard before the `drawFigure` call.

### 5. AGENTS.md documentation error — `EndNode` uses `super(true)` not `super(false)`
- **File:** `src/tree/EndNode.java`, `AGENTS.md:197`
- **Detail:** AGENTS.md documents `super(false)`, but `EndNode` actually calls
  `super(true)`. The `skipInit` parameter name implies `true` = skip, so
  `true` is semantically correct. The docs contain a typo.
- **Fix needed:** Correct AGENTS.md line 197 to say `super(true)`.

---

## Missing Logic

| Feature                              | Notes                                                  |
|--------------------------------------|--------------------------------------------------------|
| `moveFigure(GameFigure)` body        | Empty stub; core movement mechanic entirely absent     |
| Figure selection                     | When multiple figures on board, no logic to pick one   |
| "Roll 6 → place figure + roll again" | TODO comment at `Board.java:47`; not implemented       |
| "Roll 6 with figure on board → roll again" | Not implemented                                 |
| Figure enters finish lane (garage)   | No transition from main track into `GarageNode` chain  |
| Send opponent home on landing        | No collision detection or "kick" logic                 |
| Win condition                        | No check for all 4 figures in the garage               |
| `update()` else-branch               | Only handles "all figures at home"; missing "figures on board" branch |

---

## Model Gaps

| Gap                                         | File / Location                      |
|---------------------------------------------|--------------------------------------|
| `GameFigure` has no `team()` accessor        | `src/models/GameFigure.java:4` — `team` field is private with no getter |
| `Team` has two redundant rootNode accessors  | `src/models/Team.java` — both `rootNode()` and `teamRootNode()` return the same field |
| `Board.startGame()` creates unused `Random`  | `src/models/Board.java:24`           |
| `ContentNode` redundantly sets `next = new EndNode()` | `src/tree/ContentNode.java` — `super()` already does this |
| `Node.getNodeForGameFigure()` returns `null` by default | Does not delegate to `next`; breaks traversal on mixed-type lists |
| AGENTS.md `ui/` description says "currently empty" | `ui/` now has 3 files; docs are stale |
| AGENTS.md `listener/` package undocumented  | Package added in second commit, not in Directory Structure section |

---

## Next Steps (Prioritized)

1. **Fix compile error** — resolve `moveFigure()` call in `Board.update()` (`Board.java:48`)
2. **Fix duplicate `TeamRootNode`** — wire `teams[0].teamRootNode(n)` before the loop in `createBoard()` (`Board.java:76–98`)
3. **Add `GameFigure.team()` accessor** — unblocks team-color rendering in `BoardPanel`
4. **Implement `moveFigure(GameFigure)`** — walk N steps along the linked list
5. **Add repaint trigger** — connect `InputListener` → `BoardPanel.repaint()`
6. **Fix `drawAllHomes` null guard** — `BoardPanel.java:234`
7. **Implement `update()` else-branch** — handle figures already on the board
8. **Implement "roll 6" rules** — place figure from home + roll again; roll again when on board
9. **Implement finish-lane entry** — detect when a figure passes its own `GarageRootNode`
10. **Implement send-home collision** — when landing on occupied square belonging to another team
11. **Implement win condition** — all 4 figures of a team in garage = that team wins
12. **Fix architecture violation** — remove `tree → models` import (move `GameFigure` or use generics)
13. **Update AGENTS.md** — fix `super(false)` → `super(true)` typo; add `listener/` and actual `ui/` to directory structure; update dependencies table

---

## Change Log

| Date       | Change                                      | Author  |
|------------|---------------------------------------------|---------|
| 2026-03-10 | Initial commit — board data structure       | human   |
| 2026-03-10 | Add Swing UI layer (`ui/`, `listener/`)     | human   |
| 2026-03-10 | Create `memory.md` — initial analysis       | agent   |
