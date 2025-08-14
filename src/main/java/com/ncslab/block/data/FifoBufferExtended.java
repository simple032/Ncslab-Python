package com.ncslab.block.data;

import lombok.Getter;

import java.util.LinkedList;
import java.util.function.Function;

public class FifoBufferExtended<T> {
    private final LinkedList<TimeDataPair<T>> buffer;
    private final int capacity;
    private InterpolationStrategy<T> interpolationStrategy;

    public static class TimeDataPair<T> {
        @Getter
        private final double timestamp;
        @Getter
        private final T data;

        public TimeDataPair(double timestamp, T data) {
            this.timestamp = timestamp;
            this.data = data;
        }

    }

    public interface InterpolationStrategy<T> {
        T interpolate(TimeDataPair<T> lower, TimeDataPair<T> upper, double targetTime);
    }

    public FifoBufferExtended(int capacity) {
        this.buffer = new LinkedList<>();
        this.capacity = capacity;
        this.interpolationStrategy = new LinearInterpolationStrategy<>(); // 默认线性插值
    }

    public synchronized void setInterpolationStrategy(InterpolationStrategy<T> strategy) {
        this.interpolationStrategy = strategy;
    }

    public synchronized void add(double timestamp, T data) {
        if (buffer.size() >= capacity) {
            buffer.poll();
        }
        buffer.offer(new TimeDataPair<>(timestamp, data));
    }

    public synchronized T getValueByDelay(double currentTime, double delay) {
        double targetTime = currentTime - delay;
        if (buffer.isEmpty()) {
            return null;
        }

        // 找到最接近的两个点
        TimeDataPair<T> lower = null;
        TimeDataPair<T> upper = null;

        for (TimeDataPair<T> pair : buffer) {
            if (pair.getTimestamp() <= targetTime) {
                lower = pair;
            } else {
                upper = pair;
                break;
            }
        }

        // 处理边界情况
        if (lower == null) {
            return buffer.getFirst().getData(); // 所有点都在目标时间之后
        } else if (upper == null) {
            return buffer.getLast().getData();  // 所有点都在目标时间之前
        } else if (lower.getTimestamp() == targetTime) {
            return lower.getData();             // 找到精确匹配
        }

        // 使用当前插值策略进行插值
        return interpolationStrategy.interpolate(lower, upper, targetTime);
    }

    // 线性插值策略实现
    public static class LinearInterpolationStrategy<T> implements InterpolationStrategy<T> {
        @Override
        public T interpolate(TimeDataPair<T> lower, TimeDataPair<T> upper, double targetTime) {
            double t = (targetTime - lower.getTimestamp()) / (upper.getTimestamp() - lower.getTimestamp());
            return createInterpolatedValue(lower, upper, t);
        }

        protected T createInterpolatedValue(TimeDataPair<T> lower, TimeDataPair<T> upper, double t) {
            // 子类需实现具体的插值逻辑
            throw new UnsupportedOperationException("Subclasses must implement this method");
        }
    }

    // 示例：为特定数据类型实现线性插值策略
    public static class LinearDataInterpolationStrategy extends LinearInterpolationStrategy<Data> {
        @Override
        protected Data createInterpolatedValue(TimeDataPair<Data> lower, TimeDataPair<Data> upper, double t) {

            return lower.getData().plus(
                (upper.getData().minus(lower.getData())).times(new Data(t))
            );
        }
    }

    // 常量插值策略（取最近值）
    public static class NearestNeighborStrategy<T> implements InterpolationStrategy<T> {
        @Override
        public T interpolate(TimeDataPair<T> lower, TimeDataPair<T> upper, double targetTime) {
            double lowerDist = targetTime - lower.getTimestamp();
            double upperDist = upper.getTimestamp() - targetTime;
            return lowerDist < upperDist ? lower.getData() : upper.getData();
        }
    }

}
