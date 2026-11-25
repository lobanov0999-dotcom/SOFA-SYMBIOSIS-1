"""Architecture-level thought experiment for n-by-n digit multiplication.

This script compares schedules, not real hardware performance.
All three designs perform the same n*n single-digit products and combine
n shifted partial-product rows. The model assumes:
- one digit multiplication occupies one abstract multiplier lane for one stage;
- one partial-row combine occupies one abstract addition stage;
- a balanced tree combines n rows in ceil(log2(n)) stages;
- real carry propagation, routing, table decoding, fan-out, clocking,
  memory, and physical nine-level device costs are NOT modeled.

The purpose is to expose work vs. parallelism trade-offs without claiming
that a nine-level implementation is faster or more energy-efficient.
"""
from math import ceil, log2


def compare_architectures(n: int, hybrid_lanes: int = 4) -> dict:
    if not isinstance(n, int) or n < 1:
        raise ValueError("n must be a positive integer")
    if not isinstance(hybrid_lanes, int) or hybrid_lanes < 1:
        raise ValueError("hybrid_lanes must be a positive integer")

    products = n * n
    combine_stages = ceil(log2(n)) if n > 1 else 0

    # One multiplier lane, one product per stage.
    serial = {
        "multiplier_lanes": 1,
        "product_waves": products,
        "partial_row_combine_stages": combine_stages,
        "abstract_depth": products + combine_stages,
    }

    # All n*n digit products launched in one wave.
    parallel = {
        "multiplier_lanes": products,
        "product_waves": 1,
        "partial_row_combine_stages": combine_stages,
        "abstract_depth": 1 + combine_stages,
    }

    # Fixed number of lanes; remaining products are scheduled in waves.
    lanes = min(hybrid_lanes, products)
    hybrid = {
        "multiplier_lanes": lanes,
        "product_waves": ceil(products / lanes),
        "partial_row_combine_stages": combine_stages,
        "abstract_depth": ceil(products / lanes) + combine_stages,
    }

    return {
        "digits_per_operand": n,
        "single_digit_products_total": products,
        "partial_product_rows": n,
        "serial": serial,
        "fully_parallel": parallel,
        "hybrid": hybrid,
        "work_is_same": True,
        "warning": (
            "Abstract scheduling depth only; not CPU cycles, silicon area, "
            "latency, or energy. Real multi-digit addition/carry logic may "
            "dominate and must be modeled separately."
        ),
    }


def storage_comparison(n: int) -> dict:
    """Storage for two n-digit operands, excluding result and control."""
    if not isinstance(n, int) or n < 1:
        raise ValueError("n must be a positive integer")
    return {
        "base9_digits_total": 2 * n,
        "binary_encoded_storage_bits": 8 * n,  # 4 bits per 9-state symbol
        "information_lower_bound_bits": ceil(2 * n * log2(9)),
        "ideal_physical_9_level_elements": 2 * n,
        "note": (
            "The ideal multi-level element count assumes reliable physical "
            "nine-way storage; feasibility, noise margins, read/write energy "
            "and peripheral circuitry are not included."
        ),
    }


if __name__ == "__main__":
    for width in (2, 4, 8, 16):
        print(compare_architectures(width, hybrid_lanes=4))
        print(storage_comparison(width))
