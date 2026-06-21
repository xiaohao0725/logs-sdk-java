package io.github.xiaohao0725.logs.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 环形缓冲区 — 80% 自动触发 flush */
public class RingBuffer {
    private final LogEntry[] buf;
    private final int capacity;
    private int head, tail, count;
    private final Consumer<List<LogEntry>> flushFn;

    public RingBuffer(int capacity, Consumer<List<LogEntry>> flushFn) {
        this.capacity = Math.max(capacity, 100);
        this.buf = new LogEntry[this.capacity];
        this.flushFn = flushFn;
    }

    public synchronized void push(LogEntry entry) {
        buf[head] = entry;
        head = (head + 1) % capacity;
        count++;
        if (count >= capacity * 0.8) {
            flushFn.accept(drain());
        }
    }

    public synchronized List<LogEntry> flush() { return drain(); }

    public synchronized int size() { return count; }

    private synchronized List<LogEntry> drain() {
        List<LogEntry> entries = new ArrayList<>(count);
        while (count > 0) {
            if (buf[tail] != null) entries.add(buf[tail]);
            buf[tail] = null;
            tail = (tail + 1) % capacity;
            count--;
        }
        return entries;
    }
}
