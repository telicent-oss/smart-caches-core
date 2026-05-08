# Distribution Lifecycle

This module provides APIs for implementing lifecycle aware services that react to distribution lifecycle events.

## State Diagrams

The current transition rules are deliberately replay-tolerant:

- Self-transitions are legal so duplicate/replayed events are harmless.
- Forward-reachable catch-up transitions are legal so consumers can converge even if they miss intermediate updates.
- `Deleted` and `Completed` are terminal apart from idempotent self-republish.

### DistributionLifecycleState

```mermaid
stateDiagram-v2
    Unregistered --> Unregistered
    Unregistered --> Registered
    Unregistered --> Active
    Unregistered --> Withdrawn
    Unregistered --> Deleted

    Registered --> Registered
    Registered --> Active
    Registered --> Withdrawn
    Registered --> Deleted

    Active --> Active
    Active --> Withdrawn
    Active --> Deleted

    Withdrawn --> Withdrawn
    Withdrawn --> Active
    Withdrawn --> Deleted

    Deleted --> Deleted
```

### ApplicationState

```mermaid
stateDiagram-v2
    Requested --> Requested
    Requested --> InProgress
    Requested --> Failed
    Requested --> Completed

    InProgress --> InProgress
    InProgress --> Failed
    InProgress --> Completed

    Failed --> Failed
    Failed --> InProgress
    Failed --> Completed

    Completed --> Completed
```
