import unittest

from communication_model import (
    make_line, make_ring, make_fully_connected,
    shortest_route, transfer, compare_topologies,
)


class CommunicationModelTests(unittest.TestCase):
    def test_line_route(self):
        route = shortest_route(make_line(5), 0, 4)
        self.assertEqual(route, (0, 1, 2, 3, 4))

    def test_ring_shorter_wraparound(self):
        route = shortest_route(make_ring(5), 0, 4)
        self.assertEqual(route, (0, 4))

    def test_fully_connected_is_one_hop(self):
        self.assertEqual(len(shortest_route(make_fully_connected(7), 1, 6)) - 1, 1)

    def test_payload_reaches_destination_unchanged(self):
        result = transfer(make_ring(5), (3, 1, 4, 1, 5), 0, 4, "ring")
        self.assertEqual(result.payload, 3)
        self.assertEqual(result.route, (0, 4))
        self.assertEqual(result.hops, 1)
        self.assertEqual(result.symbols_transferred, 1)

    def test_same_symbol_payload_across_topologies(self):
        result = compare_topologies(6)
        self.assertEqual(
            {name: item["hops"] for name, item in result["results"].items()},
            {"line": 5, "ring": 1, "fully_connected": 1},
        )
        self.assertTrue(result["same_payload"])

    def test_transfer_validates_symbols(self):
        with self.assertRaises(ValueError):
            transfer(make_line(2), (0, 9), 0, 1, "line")

    def test_invalid_nodes_rejected(self):
        with self.assertRaises(ValueError):
            shortest_route(make_line(3), 0, 4)


if __name__ == "__main__":
    unittest.main()
