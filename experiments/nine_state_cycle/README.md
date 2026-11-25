# Nine-state cycle — experimental specification

Status: hypothesis / isolated experiment. This does not modify the SOFA SYMBIOSIS core.

## Working interpretation from the author's explanation

- The working cycle has nine states: 0, 1, 2, 3, 4, 5, 6, 7, 8.
- The written token `10` is a single logical marker: it closes the current cycle and signals the start of a new one.
- The two glyphs `1` and `0` are treated as one token in this notation, not as the ordinary decimal number ten and not automatically as two independent states.
- After the marker, the next working state is 0.

Illustrative stream:

`0 → 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → [10] → 0 → 1 → ...`

## Minimal transition rule

For a working state x in {0,...,8}:

- if x < 8, next(x) = x + 1;
- if x = 8, emit the cycle marker [10] and begin the next cycle at 0.

The marker is a notation/control token. It is not silently treated as an ordinary arithmetic value.

## What this specification does NOT claim

This notation alone does not prove fewer operations, lower memory use, faster computation, or lower energy use. A conventional binary machine can encode nine distinct states using four bits, with seven bit patterns unused. A physical nine-level device would need reliable separation of nine states and must be evaluated for noise, switching cost, and read/write complexity.

## Questions to settle before integrating anywhere

1. Is the marker [10] emitted after every sequence 0..8, or is it also a state that participates in arithmetic?
2. Does the system need to remember the cycle number, or is this a repeating modulo-nine process?
3. What do the nine values mean in a real computation: quantities, symbols, phases, or something else?
4. Which operation should be tested first (addition, comparison, encoding, or a task from the SOFA prototype)?

## Safety boundary

This is an isolated mathematical experiment. No Android app code, CORE-10, model, or existing kernel algorithm is changed by this document. Integration requires explicit approval.
