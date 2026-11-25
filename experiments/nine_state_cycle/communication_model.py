"""Toy network model for nine-state processing elements.

Each node stores one symbol 0..8. A message is routed hop-by-hop over a
line, ring, or fully connected graph. This is a deterministic software
simulation, not a model of physical latency, energy, or nine-level devices.
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
    payload: int
    route: tuple[int, ...]
    note: str = "Software route simulation only; not measured hardware performance."


def _validate_symbol(value: int) -> int:
    if not isinstance(value, int) or isinstance(value, bool) or not 0 <= value <= 8:
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


def shortest_route(graph: dict[int, tuple[int, ...]], source: int,
                   destination: int) -> tuple[int, ...]:
    if source not in graph or destination not in graph:
        raise ValueError("source and destination must be valid node IDs")
    queue = deque([source])
    previous = {source: None}
    while queue:
        node = queue.popleft()
        if node == destination:
            route = []
            while node is not None:
                route.append(node)
                node = previous[node]
            return tuple(reversed(route))
        for neighbor in graph[node]:
            if neighbor not in graph:
                raise ValueError("graph contains a link to an unknown node")
            if neighbor not in previous:
                previous[neighbor] = node
                queue.append(neighbor)
    raise ValueError("destination is unreachable")


def transfer(graph: dict[int, tuple[int, ...]], values: tuple[int, ...],
             source: int, destination: int, topology: str) -> NetworkResult:
    if len(values) != len(graph):
        raise ValueError("one symbol is required for each network node")
    for value in values:
        _validate_symbol(value)
    route = shortest_route(graph, source, destination)
    payload = values[source]
    # Simulate forwarding the unchanged symbol along every edge in the route.
    delivered = payload
    for _current, _next in zip(route, route[1:]):
        delivered = _validate_symbol(delivered)
    if delivered != payload:
        raise RuntimeError("payload changed during transfer")
    return NetworkResult(topology, len(graph), source, destination,
                         len(route) - 1, 1, delivered, route)


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
        results[name] = {
            "hops": result.hops,
            "route": result.route,
            "payload": result.payload,
        }
    return {
        "nodes": n,
        "source": source,
        "destination": destination,
        "results": results,
        "same_payload": len({item["payload"] for item in results.values()}) == 1,
        "warning": "Fewer hops do not prove lower energy or higher real speed.",
    }
