"""Minimal simulator for the nine-state cycle hypothesis.

The token [10] is an atomic control marker emitted on 8 -> 0.
The machine stores only its current working value (0..8); it does not
store a cycle number. This experiment is isolated from the Android app/core.
"""
from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class CycleState:
    value: int


@dataclass(frozen=True)
class Step:
    state: CycleState
    marker: Optional[str] = None


def advance(state: CycleState) -> Step:
    """Advance one state and emit [10] only when wrapping 8 -> 0."""
    if not isinstance(state.value, int) or not 0 <= state.value <= 8:
        raise ValueError("working value must be an integer in range 0..8")
    if state.value < 8:
        return Step(CycleState(state.value + 1))
    return Step(CycleState(0), marker="10")


def run(start: CycleState, transitions: int) -> list[Step]:
    if transitions < 0:
        raise ValueError("transitions must be non-negative")
    result: list[Step] = []
    current = start
    for _ in range(transitions):
        item = advance(current)
        result.append(item)
        current = item.state
    return result


if __name__ == "__main__":
    state = CycleState(0)
    for item in run(state, 18):
        if item.marker is not None:
            print(f"[{item.marker}]")
        print(f"value={item.state.value}")
