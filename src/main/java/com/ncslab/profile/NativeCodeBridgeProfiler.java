package com.ncslab.profile;

import java.util.Locale;

/**
 * Optional timing for the Java-side TCP message loop while the generated {@code ncslab} process streams data.
 * Uses the same enable switch as {@link SimulationStepProfiler}.
 */
public final class NativeCodeBridgeProfiler {

    private static final Object LOCK = new Object();

    private static long preambleReadNs;
    private static long messageHandleNs;
    private static long messages;

    private NativeCodeBridgeProfiler() {
    }

    public static boolean isEnabled() {
        return SimulationStepProfiler.isEnabled();
    }

    public static void reset() {
        synchronized (LOCK) {
            preambleReadNs = 0;
            messageHandleNs = 0;
            messages = 0;
        }
    }

    public static void recordMessage(long preambleReadNanos, long handleNanos) {
        if (!SimulationStepProfiler.isEnabled()) {
            return;
        }
        synchronized (LOCK) {
            preambleReadNs += preambleReadNanos;
            messageHandleNs += handleNanos;
            messages++;
        }
    }

    public static void logSummary(String bridgeName) {
        if (!SimulationStepProfiler.isEnabled()) {
            return;
        }
        long pr;
        long mh;
        long m;
        synchronized (LOCK) {
            pr = preambleReadNs;
            mh = messageHandleNs;
            m = messages;
        }
        if (m == 0) {
            System.out.printf(Locale.US, "[NativeCodeBridgeProfiler] %s: no messages recorded%n", bridgeName);
            return;
        }
        long total = pr + mh;
        System.out.printf(Locale.US,
                "[NativeCodeBridgeProfiler] %s messages=%d wall=%.3f ms (preambleRead=%.1f%% handle=%.1f%%)%n",
                bridgeName,
                m,
                total / 1_000_000.0,
                100.0 * pr / total,
                100.0 * mh / total);
        System.out.printf(Locale.US,
                "[NativeCodeBridgeProfiler] %s per-message mean us: preambleRead=%.3f handle=%.3f%n",
                bridgeName,
                (pr / (double) m) / 1000.0,
                (mh / (double) m) / 1000.0);
    }
}
