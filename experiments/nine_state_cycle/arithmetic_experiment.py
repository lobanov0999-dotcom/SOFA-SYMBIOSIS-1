"""Arithmetic and comparison experiment for nine-state symbols.

The nine-state notation uses symbols 0..8. The token [10] remains a
control marker for cycle rollover and is NOT an arithmetic digit.
This is an abstract operation-count comparison, not a hardware benchmark.

For each pair of one-symbol values (a,b), compare:
- nine-state digit arithmetic: one symbol-pair addition, threshold/carry test;
- binary encoding: each symbol represented with 4 bits, then a 4-bit ripple add.
Counts are structural estimates only. They do not imply physical speed/energy.
"""


def add_nine_digit(a: int, b: int) -> tuple[int, int]:
    """Return (result_digit, carry) for one base-9 digit addition."""
    _check_digit(a)
    _check_digit(b)
    total = a + b
    return total % 9, total // 9


def compare_nine_digit(a: int, b: int) -> int:
    """Return -1, 0, or 1 for two nine-state symbols."""
    _check_digit(a)
    _check_digit(b)
    return (a > b) - (a < b)


def _check_digit(value: int) -> None:
    if not isinstance(value, int) or not 0 <= value <= 8:
        raise ValueError("digit must be an integer from 0 through 8")


def add_binary_encoded(a: int, b: int) -> tuple[int, int, int]:
    """4-bit ripple addition; return (low_4_bits, carry, full_adder_stages)."""
    _check_digit(a)
    _check_digit(b)
    carry = 0
    result = 0
    for bit in range(4):
        abit = (a >> bit) & 1
        bbit = (b >> bit) & 1
        total = abit + bbit + carry
        result |= (total & 1) << bit
        carry = (total >> 1) & 1
    return result, carry, 4


def compare_binary_encoded(a: int, b: int) -> tuple[int, int]:
    """Compare 4-bit encodings from MSB to LSB; return (result, bit_checks)."""
    _check_digit(a)
    _check_digit(b)
    for bit in range(3, -1, -1):
        abit = (a >> bit) & 1
        bbit = (b >> bit) & 1
        if abit != bbit:
            return (1 if abit else -1), 4 - bit
    return 0, 4


def exhaustive_report() -> dict:
    pairs = [(a, b) for a in range(9) for b in range(9)]
    add_cases = 0
    carry_cases = 0
    max_sum = 0
    compare_checks = 0
    binary_add_stages = 0
    binary_compare_checks = 0

    for a, b in pairs:
        digit, carry = add_nine_digit(a, b)
        assert digit + 9 * carry == a + b
        add_cases += 1
        carry_cases += carry
        max_sum = max(max_sum, a + b)

        nine_cmp = compare_nine_digit(a, b)
        binary_cmp, checks = compare_binary_encoded(a, b)
        assert nine_cmp == binary_cmp
        compare_checks += 1  # one abstract symbol comparison
        binary_compare_checks += checks

        binary_sum, binary_carry, stages = add_binary_encoded(a, b)
        assert binary_sum + (binary_carry << 4) == a + b
        binary_add_stages += stages

    return {
        "tested_pairs": len(pairs),
        "nine_state_add_symbol_pair_operations": add_cases,
        "nine_state_carry_events": carry_cases,
        "nine_state_comparisons": compare_checks,
        "binary_4bit_full_adder_stages": binary_add_stages,
        "binary_comparator_bit_checks": binary_compare_checks,
        "maximum_single_digit_sum": max_sum,
        "note": "Abstract counts only; physical latency, area, and energy are unmeasured.",
    }


if __name__ == "__main__":
    print(exhaustive_report())
