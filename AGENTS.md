# AGENTS.md — SorryBoardGame

Coding-agent reference for this repository. Read this before making changes.

---

## Project Overview

| Property        | Value                                      |
|-----------------|--------------------------------------------|
| Language        | Java 25 (Temurin / Eclipse Adoptium)       |
| Language level  | JDK_25 (preview features enabled)          |
| Build system    | None — IntelliJ IDEA native module (`.iml`)|
| Dependencies    | JDK stdlib only (`java.awt.Color`)         |
| Entry point     | `src/Main.java` (unnamed class)            |
| Test framework  | None configured yet                        |
| CI/CD           | None                                       |

---

## Directory Structure

```
src/
├── Main.java                  # Entry point — unnamed class, void main()
├── exceptions/                # Custom RuntimeException subclasses
├── models/                    # Domain objects (Board, Team, GameFigure, builders)
├── tree/                      # Node types for the board's linked-list structure
└── ui/                        # Reserved for future UI layer (currently empty)
```

---

## Build Commands

There is no Gradle or Maven setup. Use `javac`/`java` directly.

### Compile everything

```bash
javac --release 25 --enable-preview -d out \
  src/exceptions/*.java \
  src/tree/*.java \
  src/models/*.java \
  src/Main.java
```

> `--enable-preview` is required because `Main.java` uses the **unnamed class /
> instance main method** feature (JEP 463), which requires preview in JDK 25.

### Run

```bash
java --enable-preview -cp out Main
```

### Compile and run in one step

```bash
javac --release 25 --enable-preview -d out \
  src/exceptions/*.java src/tree/*.java src/models/*.java src/Main.java \
  && java --enable-preview -cp out Main
```

---

## Testing

No test framework is configured yet. When tests are added, use **JUnit 5**.

### Recommended setup (when adding tests)

- Place tests under `src/test/` mirroring the source package structure.
- Add JUnit 5 (`junit-platform-launcher`, `junit-jupiter-api`,
  `junit-jupiter-engine`) JARs to a `lib/` directory.

### Compile a single test (example)

```bash
javac --release 25 --enable-preview -cp out:lib/* \
  -d out src/test/tree/NodeTest.java
```

### Run a single test class (example)

```bash
java --enable-preview -cp out:lib/* \
  org.junit.platform.console.ConsoleLauncher \
  --select-class=tree.NodeTest
```

---

## Code Style Guidelines

### Formatting

- **Indentation**: 4 spaces — never tabs.
- **Line length**: no hard limit, but keep lines readable (aim for ≤ 120 chars).
- **Opening braces**: same line as the declaration (K&R style).
- **Blank lines**: one blank line between methods; two between top-level class
  members of different kinds (fields vs. methods).
- **Trailing whitespace**: none.

### Naming Conventions

| Element              | Convention                         | Example                   |
|----------------------|------------------------------------|---------------------------|
| Classes / interfaces | `PascalCase`                       | `BoardBuilder`, `TeamNode`|
| Methods / fields     | `camelCase`                        | `createBoard`, `rootNode` |
| Constants            | `UPPER_SNAKE_CASE`                 | `MAX_TEAMS`               |
| Packages             | all lowercase, no underscores      | `models`, `tree`          |
| Local variables      | `camelCase`, descriptive           | `prev`, `nextTeam`        |

### Accessor Style

Do **not** use `get`/`set` prefixes. Accessor methods use the field name directly:

```java
// Correct
public Node next()             { return next; }
public void next(Node node)    { this.next = node; }
public Team team()             { return team; }

// Wrong
public Node getNext()          { ... }
public void setNext(Node node) { ... }
```

### Immutability

Prefer `private final` for fields that are set once in the constructor and never
reassigned. Use mutable fields only when mutation is semantically required
(e.g. `TeamRootNode rootNode` in `Team`, which is wired up after construction).

```java
// Preferred
private final int id;
private final Color color;

// Acceptable when post-construction wiring is unavoidable
private TeamRootNode rootNode;
```

### Imports

- **No wildcard imports** except `java.awt.*` in `Main.java` (acceptable for
  well-known JDK packages used heavily).
- Import each class explicitly everywhere else.
- Order: `java.*`, `javax.*`, blank line, third-party, blank line, project
  packages.
- Remove unused imports before committing.

---

## Error Handling

- Custom exceptions **extend `RuntimeException`** (unchecked) — do not use
  checked exceptions.
- All custom exception classes live in the `exceptions` package.
- Exception messages are written in **German**.
- Include a descriptive prefix in the message:

```java
// Correct
super("Ungültige Baumstruktur: " + message);

// Wrong — no prefix, no German
super(message);
```

- Do **not** swallow exceptions silently. Let them propagate unless there is a
  meaningful recovery strategy at the call site.

---

## Architecture Notes

### Board as a Circular Linked List

The game board is modelled as a singly-linked list of `Node` subtypes. The list
is circular: the last `TeamRootNode` of the final team has its `next` set to the
first `TeamRootNode` (teams[0]).

Layout per team (repeated N times):

```
TeamRootNode → ContentNode ×8 → GarageRootNode → (next team's TeamRootNode)
                                      └─ GarageNode ×4 (side chain)
```

### Null-Object Sentinel (`EndNode`)

`Node` initialises `next = new EndNode()` by default. `EndNode.next()` returns
`this`, so traversal never hits a `NullPointerException`.

**Critical:** `EndNode` must call `super(false)` (the `skipInit` constructor on
`Node`) to avoid infinite recursion. Never change this without understanding the
recursion risk — see `Node.java` for the protected constructor.

```java
// EndNode.java — do not change this constructor call
public EndNode() {
    super(false); // prevents Node() from calling new EndNode() again
}
```

### Builder Pattern

Use the Builder pattern for any class with multiple optional or ordered
construction steps. See `BoardBuilder` as the canonical example:

```java
Board board = new BoardBuilder()
        .addTeam(Color.RED)
        .addTeam(Color.BLUE)
        .build();
```

### Package Responsibilities

| Package      | Responsibility                                              |
|--------------|-------------------------------------------------------------|
| `models`     | Domain logic — `Board`, `Team`, `GameFigure`, builders      |
| `tree`       | Data structure — all `Node` subtypes                        |
| `exceptions` | Custom unchecked exceptions                                 |
| `ui`         | Future presentation layer — keep UI concerns out of models  |

Dependencies must only flow **inward**: `ui` → `models` → `tree`/`exceptions`.
The `tree` and `exceptions` packages must not import from `models` or `ui`.

---

## Git Conventions

- Branch: `master`
- Commit messages: imperative mood, concise (e.g. `Fix StackOverflow in EndNode constructor`)
- Do not commit the `out/` directory (already in `.gitignore`)
