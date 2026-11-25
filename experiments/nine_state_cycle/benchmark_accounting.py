"""Reproducible accounting experiment for the nine-state cycle.

This counts abstract state transitions and marker emissions. It does NOT
benchmark CPU time, gate count, physical multi-level hardware, or energy.
The nine-state register is assumed to be represented either as:
- a conventional binary encoding (4 storage bits for 9 distinguishable states),
- an idealized single 9-level physical element (hardware feasibility unproven).

The reference binary counter is a separate ordinary binary integer counter.
It retains the total count, so its storage requirement grows with the count.
That is intentionally reported separately because the modulo-nine model
forgets completed cycles and therefore does not preserve the same information.
"""
from math import ceil, log2


def run_mod9(inputs: int) -> dict:
    state = 0
    markers = 0
    visited = set()
    transitions = 0
    for _ in range(inputs):
        visited.add(state)
        transitions += 1
        if state == 8:
            state = 0
            markers += 1
        else:
            state += 1
    return {
        "inputs": inputs,
        "abstract_transitions": transitions,
        "distinct_states_visited": len(visited),
        "markers": markers,
        "final_state": state,
        "binary_encoded_state_bits": 4,
        "ideal_information_bits_for_9_states": log2(9),
        "idealized_9_level_elements": 1,
    }


def run_binary_total_counter(inputs: int) -> dict:
    # A conventional binary counter that retains the total number of inputs.
    return {
        "inputs": inputs,
        "abstract_increments": inputs,
        "final_count": inputs,
        "storage_bits": max(1, ceil(log2(inputs + 1))),
    }


if __name__ == "__main__":
    n = 1000
    print("Modulo-nine state machine:", run_mod9(n))
    print("Binary total counter:", run_binary_total_counter(n))
