# Nine-state cycle — experimental specification

Status: isolated hypothesis. This experiment does not modify the SOFA SYMBIOSIS Android app or core.

## Agreed interpretation

- The working cycle has nine values: 0 through 8.
- After value 8, the machine emits the atomic control token [10] and returns to 0.
- [10] is not an arithmetic digit and is not one of the nine working values.
- The machine does not store a cycle number. It stores only its current value (0..8).
- The marker is an event emitted at rollover; the next working state is 0.

Illustrative stream:

`0 → 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → [10] → 0 → 1 → ...`

## Transition rule

For a working value x in {0,...,8}:

- if x < 8, next(x) = x + 1 and no marker is emitted;
- if x = 8, next(x) = 0 and the control marker [10] is emitted.

## Arithmetic is a separate layer

The arithmetic prototype uses ordinary positional base-9 digits 0..8. In that arithmetic context, `10₉` is the usual representation of decimal nine. That notation must not be confused with the atomic rollover control token [10] defined above.

## What this specification does NOT claim

This notation alone does not prove fewer operations, lower memory use, faster computation, or lower energy use. A conventional binary machine can encode nine distinct states using four bits, with seven bit patterns unused. A physical nine-level device would need reliable separation of nine states and must be evaluated for noise, switching cost, read/write complexity, and peripheral circuitry.

## Safety boundary

This is an isolated software experiment. No Android app code, CORE-10, model, or existing kernel algorithm is changed. Integration requires explicit approval.
