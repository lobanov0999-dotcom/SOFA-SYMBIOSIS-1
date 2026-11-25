"""Tests for the modulo-nine control-marker model.

The marker '10' is emitted only on the transition 8 -> 0.
It is not stored as a state, and no cycle counter is retained.
"""
import unittest

from cycle_model import CycleState, advance, run


class NineStateCycleTests(unittest.TestCase):
    def test_all_nine_states_are_reachable(self):
        state = CycleState(cycle=0, value=0)
        seen = [state.value]
        for _ in range(8):
            state = advance(state).state
            seen.append(state.value)
        self.assertEqual(seen, list(range(9)))

    def test_marker_only_on_rollover(self):
        for value in range(8):
            step = advance(CycleState(cycle=0, value=value))
            self.assertIsNone(step.marker)
            self.assertEqual(step.state.value, value + 1)

        step = advance(CycleState(cycle=0, value=8))
        self.assertEqual(step.marker, "10")
        self.assertEqual(step.state.value, 0)

    def test_no_cycle_memory(self):
        state = CycleState(cycle=0, value=0)
        steps = run(state, 27)
        self.assertEqual(len(steps), 27)
        self.assertEqual([s.state.value for s in steps],
                         [((i + 1) % 9) for i in range(27)])
        self.assertEqual([i for i, s in enumerate(steps, 1)
                          if s.marker == "10"], [9, 18, 27])

    def test_invalid_state_rejected(self):
        with self.assertRaises(ValueError):
            advance(CycleState(cycle=0, value=9))


if __name__ == "__main__":
    unittest.main()
