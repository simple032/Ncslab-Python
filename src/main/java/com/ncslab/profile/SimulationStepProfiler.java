package com.ncslab.profile;

import java.util.Locale;

/**
 * Optional nanosecond aggregates for {@link com.ncslab.ncslablink.SimulationModel} fixed-step integration.
 * Enable with {@code -Dncslab.sim.profile=true} or environment variable {@code NCSLAB_SIM_PROFILE=1}.
 */
public final class SimulationStepProfiler {

    private static final Object LOCK = new Object();
    private static volatile boolean enabled;

    private static long integrateNs;
    private static long outputsNs;
    private static long circuitNs;
    private static long discreteNs;
    private static long scopeFlushNs;
    private static long steps;

    static {
        enabled = Boolean.getBoolean("ncslab.sim.profile")
                || "1".equalsIgnoreCase(System.getenv("NCSLAB_SIM_PROFILE"));
    }

    private SimulationStepProfiler() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void reset() {
        synchronized (LOCK) {
            integrateNs = 0;
            outputsNs = 0;
            circuitNs = 0;
            discreteNs = 0;
            scopeFlushNs = 0;
            steps = 0;
        }
    }

    public static void recordFixedStep(
            long integrateNanos,
            long outputsNanos,
            long circuitNanos,
            long discreteNanos,
            long scopeFlushNanos) {
        if (!enabled) {
            return;
        }
        synchronized (LOCK) {
            integrateNs += integrateNanos;
            outputsNs += outputsNanos;
            circuitNs += circuitNanos;
            discreteNs += discreteNanos;
            scopeFlushNs += scopeFlushNanos;
            steps++;
        }
    }

    public static void logSummary(String tag, int stepsCompleted) {
        if (!enabled) {
            return;
        }
        long i;
        long o;
        long c;
        long d;
        long s;
        long n;
        synchronized (LOCK) {
            i = integrateNs;
            o = outputsNs;
            c = circuitNs;
            d = discreteNs;
            s = scopeFlushNs;
            n = steps;
        }
        long total = i + o + c + d + s;
        if (n == 0) {
            System.out.printf(Locale.US,
                    "[SimulationStepProfiler] %s: no instrumented steps (loop may have exited early; reported steps=%d)%n",
                    tag, stepsCompleted);
            return;
        }
        System.out.printf(Locale.US,
                "[SimulationStepProfiler] %s steps=%d completed=%d total=%.3f ms (integrate=%.1f%% outputs=%.1f%% circuit=%.1f%% discrete=%.1f%% scopeFlush=%.1f%%)%n",
                tag,
                n,
                stepsCompleted,
                total / 1_000_000.0,
                100.0 * i / total,
                100.0 * o / total,
                100.0 * c / total,
                100.0 * d / total,
                100.0 * s / total);
        System.out.printf(Locale.US,
                "[SimulationStepProfiler] %s per-step mean us: integrate=%.3f outputs=%.3f circuit=%.3f discrete=%.3f scopeFlush=%.3f%n",
                tag,
                (i / (double) n) / 1000.0,
                (o / (double) n) / 1000.0,
                (c / (double) n) / 1000.0,
                (d / (double) n) / 1000.0,
                (s / (double) n) / 1000.0);
    }
}
