import unittest

from nine_state_alu import (
    add_digit, subtract_digit, compare_digit,
    add_number, compare_number, format_number,
)


def value(digits):
    return sum(d * (9 ** i) for i, d in enumerate(digits))


class NineStateAluTests(unittest.TestCase):
    def test_all_single_digit_additions(self):
        for a in range(9):
            for b in range(9):
                digit, carry = add_digit(a, b)
                self.assertEqual(digit + 9 * carry, a + b)

    def test_all_single_digit_subtractions(self):
        for a in range(9):
            for b in range(9):
                digit, borrow = subtract_digit(a, b)
                self.assertEqual(digit - 9 * borrow, a - b)

    def test_all_single_digit_comparisons(self):
        for a in range(9):
            for b in range(9):
                self.assertEqual(compare_digit(a, b), (a > b) - (a < b))

    def test_carry_in_and_borrow_in(self):
        for a in range(9):
            for b in range(9):
                digit, carry = add_digit(a, b, 1)
                self.assertEqual(digit + 9 * carry, a + b + 1)
                digit, borrow = subtract_digit(a, b, 1)
                self.assertEqual(digit - 9 * borrow, a - b - 1)

    def test_multi_digit_addition(self):
        cases = [
            ((8,), (1,)),
            ((8, 8), (1,)),
            ((8, 8), (8, 8)),
            ((2, 5, 1), (7, 3)),
        ]
        for a, b in cases:
            result = add_number(a, b)
            self.assertEqual(value(result), value(a) + value(b))

    def test_multi_digit_comparison(self):
        cases = [
            ((8,), (1,), 1),
            ((0, 1), (8,), 1),
            ((8, 8), (8, 8), 0),
            ((1,), (2, 0), -1),
        ]
        for a, b, expected in cases:
            self.assertEqual(compare_number(a, b), expected)

    def test_formatting(self):
        self.assertEqual(format_number((1, 0, 8)), "801")
        self.assertEqual(format_number((0, 0, 0)), "0")


    def test_all_single_digit_multiplications(self):
        from nine_state_alu import multiply_digit
        for a in range(9):
            for b in range(9):
                digit, carry = multiply_digit(a, b)
                self.assertEqual(digit + 9 * carry, a * b)

    def test_multi_digit_multiplication(self):
        from nine_state_alu import multiply_number
        cases = [
            ((8,), (8,)),
            ((8, 8), (8,)),
            ((8, 8), (8, 8)),
            ((2, 5, 1), (7, 3)),
            ((0,), (8, 8, 8)),
        ]
        for a, b in cases:
            result = multiply_number(a, b)
            self.assertEqual(value(result), value(a) * value(b))


if __name__ == "__main__":
    unittest.main()
