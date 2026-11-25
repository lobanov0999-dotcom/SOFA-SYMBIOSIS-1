"""Nine-state lookup-table ALU prototype.

Digits are the nine working symbols 0..8. The cycle marker [10] is a
control-flow token only and is not used as an arithmetic digit or carry.
Arithmetic carry/borrow are separate one-bit control outputs.

This is a software model of a lookup-table ALU, not a claim about
multi-level physical hardware performance.
"""

DIGITS = tuple(range(9))

# Each cell is (result_digit, carry_out), with carry_out in {0, 1}.
ADD_TABLE = tuple(
    tuple(((a + b) % 9, (a + b) // 9) for b in DIGITS)
    for a in DIGITS
)

# Each cell is (result_digit, borrow_out), with borrow_out in {0, 1}.
SUB_TABLE = tuple(
    tuple(((a - b) % 9, 1 if a < b else 0) for b in DIGITS)
    for a in DIGITS
)

# -1 means a < b, 0 means equal, +1 means a > b.
COMPARE_TABLE = tuple(
    tuple((a > b) - (a < b) for b in DIGITS)
    for a in DIGITS
)


def _digit(value: int) -> int:
    if not isinstance(value, int) or not 0 <= value <= 8:
        raise ValueError("digit must be an integer from 0 through 8")
    return value


def add_digit(a: int, b: int, carry_in: int = 0) -> tuple[int, int]:
    """Add two base-9 digits and an incoming carry."""
    _digit(a)
    _digit(b)
    if carry_in not in (0, 1):
        raise ValueError("carry_in must be 0 or 1")
    first, carry = ADD_TABLE[a][b]
    if carry_in:
        result, extra = ADD_TABLE[first][1]
        return result, carry | extra
    return first, carry


def subtract_digit(a: int, b: int, borrow_in: int = 0) -> tuple[int, int]:
    """Subtract two base-9 digits and an incoming borrow."""
    _digit(a)
    _digit(b)
    if borrow_in not in (0, 1):
        raise ValueError("borrow_in must be 0 or 1")
    first, borrow = SUB_TABLE[a][b]
    if borrow_in:
        result, extra = SUB_TABLE[first][1]
        return result, borrow | extra
    return first, borrow


def compare_digit(a: int, b: int) -> int:
    """Compare two single base-9 digits: -1, 0, or +1."""
    _digit(a)
    _digit(b)
    return COMPARE_TABLE[a][b]


def add_number(a_digits: tuple[int, ...], b_digits: tuple[int, ...]) -> tuple[int, ...]:
    """Add little-endian base-9 digit tuples; return little-endian result."""
    if not a_digits or not b_digits:
        raise ValueError("numbers must contain at least one digit")
    width = max(len(a_digits), len(b_digits))
    result = []
    carry = 0
    for i in range(width):
        a = _digit(a_digits[i]) if i < len(a_digits) else 0
        b = _digit(b_digits[i]) if i < len(b_digits) else 0
        digit, carry = add_digit(a, b, carry)
        result.append(digit)
    if carry:
        result.append(carry)
    return tuple(result)


def compare_number(a_digits: tuple[int, ...], b_digits: tuple[int, ...]) -> int:
    """Compare little-endian base-9 digit tuples."""
    a = _normalized(a_digits)
    b = _normalized(b_digits)
    if len(a) != len(b):
        return 1 if len(a) > len(b) else -1
    for x, y in zip(reversed(a), reversed(b)):
        result = compare_digit(x, y)
        if result:
            return result
    return 0


def _normalized(digits: tuple[int, ...]) -> tuple[int, ...]:
    if not digits:
        raise ValueError("number must contain at least one digit")
    values = tuple(_digit(x) for x in digits)
    end = len(values)
    while end > 1 and values[end - 1] == 0:
        end -= 1
    return values[:end]


def format_number(digits: tuple[int, ...]) -> str:
    """Format a little-endian digit tuple in conventional display order."""
    return "".join(str(x) for x in reversed(_normalized(digits)))
