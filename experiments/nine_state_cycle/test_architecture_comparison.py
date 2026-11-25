import unittest

from architecture_comparison import compare_architectures, storage_comparison


class ArchitectureComparisonTests(unittest.TestCase):
    def test_all_architectures_do_same_product_work(self):
        for n in (1, 2, 4, 8, 16):
            result = compare_architectures(n, hybrid_lanes=4)
            self.assertEqual(result["single_digit_products_total"], n * n)
            self.assertTrue(result["work_is_same"])
            self.assertLessEqual(result["fully_parallel"]["product_waves"],
                                 result["hybrid"]["product_waves"])
            self.assertLessEqual(result["hybrid"]["product_waves"],
                                 result["serial"]["product_waves"])

    def test_expected_abstract_depths(self):
        expected = {
            2: (5, 2, 2),
            4: (18, 3, 6),
            8: (67, 4, 19),
            16: (260, 5, 68),
        }
        for n, depths in expected.items():
            result = compare_architectures(n, hybrid_lanes=4)
            actual = (
                result["serial"]["abstract_depth"],
                result["fully_parallel"]["abstract_depth"],
                result["hybrid"]["abstract_depth"],
            )
            self.assertEqual(actual, depths)

    def test_storage_accounting(self):
        self.assertEqual(storage_comparison(4)["binary_encoded_storage_bits"], 32)
        self.assertEqual(storage_comparison(4)["information_lower_bound_bits"], 26)


if __name__ == "__main__":
    unittest.main()
