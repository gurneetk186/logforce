# LogForge – Concurrent Log Processing Engine

LogForge is a Java-based log processing engine built to explore performance differences between sequential and multi-threaded execution. The project analyzes large log files and benchmarks how workload distribution across threads improves execution time.

## What It Does

- Parses log files and counts:
  - ERROR entries
  - WARNING entries
  - Unique IP addresses
  - HTTP status codes
- Implements a single-threaded baseline
- Implements a multi-threaded version using ExecutorService
- Measures execution time using System.nanoTime()
- Compares performance between approaches

## Why I Built This

I wanted to better understand how Java handles concurrency and how thread pools can improve performance for CPU-bound workloads. This project helped me explore:

- Callable vs Runnable
- Future result handling
- Work chunking strategies
- Thread pool sizing
- Parallel result aggregation

## Sample Benchmark

On a test log file:

Single-threaded execution: ~31 ms  
Multi-threaded execution (4 threads): ~4 ms  

This demonstrated a significant speedup by distributing workload across multiple cores.

## How to Run

```bash
javac LogProcessor.java
java LogProcessor
