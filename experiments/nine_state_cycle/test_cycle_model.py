"""Tests for the nine-state cycle with a control-only [10] marker."""
import unittest

from cycle_model import CycleState, advance, run


class NineStateCycleTests(unittest.TestCase):
    def test_all_nine_states_are_reachable(self):
        state = CycleState(0)
        seen = [state.value]
        for _ in range(8):
            state = advance(state).state
            seen.append(state.value)
        self.assertEqual(seen, list(range(9)))

    def test_marker_only_on_rollover(self):
        for value in range(8):
            step = advance(CycleState(value))
            self.assertIsNone(step.marker)
            self.assertEqual(step.state.value, value + 1)
        step = advance(CycleState(8))
        self.assertEqual(step.marker, "10")
        self.assertEqual(step.state.value, 0)

    def test_repeats_without_cycle_counter(self):
        steps = run(CycleState(0), 27)
        self.assertEqual(len(steps), 27)
        self.assertEqual([s.state.value for s in steps],
                         [((i + 1) % 9) for i in range(27)])
        self.assertEqual([i for i, s in enumerate(steps, 1)
                          if s.marker == "10"], [9, 18, 27])
        self.assertEqual(set(vars(steps[-1].state)), {"value"})

    def test_invalid_state_rejected(self):
        for value in (-1, 9, 1.5, "2"):
            with self.subTest(value=value):
                with self.assertRaises(ValueError):
                    advance(CycleState(value))


if __name__ == "__main__":
    unittest.main()
