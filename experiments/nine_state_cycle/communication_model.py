"""Toy network model for nine-state processing elements.

Each node stores one symbol 0..8. Nodes exchange symbols over links.
This models communication topology and hop count only; it does NOT model
physical nine-level electronics, clock timing, energy, noise, or real CPUs.
"""
from collections import deque
from dataclasses import dataclass


@dataclass(frozen=True)
class NetworkResult:
    topology: str
    nodes: int
    source: int
    destination: int
    hops: int
    symbols_transferred: int
    note: str = "Hop-count model only; not measured hardware latency."


def _validate_symbol(value: int) -> int:
    if not isinstance(value, int) or not 0 <= value <= 8:
        raise ValueError("each processing element must hold a symbol from 0 through 8")
    return value


def make_line(n: int) -> dict[int, tuple[int, ...]]:
    if not isinstance(n, int) or n < 1:
        raise ValueError("n must be a positive integer")
    return {i: tuple(j for j in (i - 1, i + 1) if 0 <= j < n)
            for i in range(n)}


def make_ring(n: int) -> dict[int, tuple[int, ...]]:
    if not isinstance(n, int) or n < 1:
        raise ValueError("n must be a positive integer")
    if n == 1:
        return {0: ()}
    return {i: tuple(sorted({(i - 1) % n, (i + 1) % n} - {i}))
            for i in range(n)}


def make_fully_connected(n: int) -> dict[int, tuple[int, ...]]:
    if not isinstance(n, int) or n < 1:
        raise ValueError("n must be a positive integer")
    return {i: tuple(j for j in range(n) if j != i) for i in range(n)}


def shortest_hops(graph: dict[int, tuple[int, ...]], source: int,
                  destination: int) -> int:
    if source not in graph or destination not in graph:
        raise ValueError("source and destination must be valid node IDs")
    queue = deque([(source, 0)])
    visited = {source}
    while queue:
        node, distance = queue.popleft()
        if node == destination:
            return distance
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append((neighbor, distance + 1))
    raise ValueError("destination is unreachable")


def transfer(graph: dict[int, tuple[int, ...]], values: tuple[int, ...],
             source: int, destination: int, topology: str) -> NetworkResult:
    if len(values) != len(graph):
        raise ValueError("one symbol is required for each network node")
    for value in values:
        _validate_symbol(value)
    hops = shortest_hops(graph, source, destination)
    return NetworkResult(topology, len(graph), source, destination, hops, 1)


def compare_topologies(n: int, source: int = 0,
                       destination: int | None = None) -> dict:
    if not isinstance(n, int) or n < 1:
        raise ValueError("n must be a positive integer")
    if destination is None:
        destination = n - 1
    values = tuple(i % 9 for i in range(n))
    networks = (
        ("line", make_line(n)),
        ("ring", make_ring(n)),
        ("fully_connected", make_fully_connected(n)),
    )
    results = {}
    for name, graph in networks:
        result = transfer(graph, values, source, destination, name)
        results[name] = result.hops
    return {
        "nodes": n,
        "source": source,
        "destination": destination,
        "hop_counts": results,
        "same_payload": True,
        "warning": "Fewer hops do not prove lower energy or higher real speed.",
    }
