

## Arithmetic prototype (isolated)

The file `nine_state_alu.py` defines lookup tables for:
- addition of two symbols (result digit plus arithmetic carry);
- subtraction of two symbols (result digit plus arithmetic borrow);
- comparison of two symbols.

It also provides multi-digit base-9 addition and comparison. Digits are stored least-significant first internally. The cycle marker `[10]` is not an arithmetic digit; arithmetic carry/borrow are separate outputs.

The file `test_nine_state_alu.py` exhaustively checks all 81 single-digit pairs, including carry-in and borrow-in, and includes multi-digit examples.

Important limitation: one table lookup is an abstract operation count. A physical implementation of the table needs decoding, storage, and selection logic. This software prototype does not demonstrate that it is faster, smaller, or more energy-efficient than binary logic.
