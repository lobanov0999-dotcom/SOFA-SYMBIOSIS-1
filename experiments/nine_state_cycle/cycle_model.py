"""Minimal simulator for the nine-state cycle hypothesis.

The string "10" is an atomic logical marker here, not the decimal integer 10.
This is an isolated experiment; it is not wired into the Android app or core.
"""
from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class CycleState:
    cycle: int
    value: int  # working values are 0..8


@dataclass(frozen=True)
class Step:
    state: CycleState
    marker: Optional[str] = None


def advance(state: CycleState) -> Step:
    """Advance one working state; emit the atomic '10' marker on rollover."""
    if not 0 <= state.value <= 8:
        raise ValueError("working value must be in range 0..8")
    if state.value < 8:
        return Step(CycleState(state.cycle, state.value + 1))
    return Step(CycleState(state.cycle + 1, 0), marker="10")


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
    state = CycleState(cycle=0, value=0)
    for item in run(state, 18):
        if item.marker is not None:
            print(f"[{item.marker}]")
        print(f"cycle={item.state.cycle}, value={item.state.value}")
