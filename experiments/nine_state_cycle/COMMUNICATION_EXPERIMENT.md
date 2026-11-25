# Communication topology experiment

This experiment asks how processing elements that hold nine-state symbols
could exchange data through different connection layouts.

## Model
- Each node stores one symbol in the range 0..8.
- A transfer sends one symbol from a source node to a destination node.
- The model counts the minimum number of links (hops).
- It compares a line, a ring, and a fully connected network.

## What this can show
It can compare graph distance for the same payload and node count. For example,
a ring can shorten a transfer between the two ends of a line.

## What this cannot show
Hop count is not physical latency, energy, throughput, area, or reliability.
A fully connected network requires many links and routing resources. Real
hardware needs measurements and a model of link width, arbitration, buffering,
clocking, fan-out, error correction, and the cost of encoding nine states.

The nine-state alphabet and the network topology are separate design choices:
using symbols 0..8 does not automatically make communication faster.
The control marker [10] remains separate from data symbols.
