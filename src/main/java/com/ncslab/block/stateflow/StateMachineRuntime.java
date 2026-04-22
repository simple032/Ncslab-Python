package com.ncslab.block.stateflow;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Stateflow 状态机运行时上下文。
 *
 * <p>管理状态机在仿真期间的运行时状态，包括：</p>
 * <ul>
 *   <li>当前激活的状态</li>
 *   <li>待处理的事件队列</li>
 *   <li>变量当前值</li>
 *   <li>历史状态（用于 History Junction）</li>
 *   <li>当前仿真步数</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class StateMachineRuntime {

    /** 所属 Chart */
    private StateflowChart chart;

    /** 当前激活的状态（按 ID 索引） */
    private final Map<String, State> activeStates = new LinkedHashMap<>();

    /** 所有状态（按 ID 索引） */
    private final Map<String, State> stateMap = new LinkedHashMap<>();

    /** 所有转移（按 ID 索引） */
    private final Map<String, Transition> transitionMap = new LinkedHashMap<>();

    /** 变量当前值（按名称索引） */
    private final Map<String, Object> variableValues = new LinkedHashMap<>();

    /** 事件队列 */
    private final Queue<String> eventQueue = new LinkedList<>();

    /** 浅历史记录（状态 ID → 上一个子状态 ID） */
    private final Map<String, String> shallowHistory = new HashMap<>();

    /** 深历史记录（状态 ID → 完整的活跃子状态集合） */
    private final Map<String, Set<String>> deepHistory = new HashMap<>();

    /** 当前仿真步数 */
    private long currentStep = 0;

    /** 是否正在初始化阶段 */
    private boolean initializing = false;

    /**
     * 初始化运行时上下文。
     *
     * @param chart 所属的 StateflowChart
     */
    public void initialize(StateflowChart chart) {
        this.chart = chart;
        this.currentStep = 0;
        this.initializing = true;

        // 加载所有状态和转移
        StateflowData data = chart.getStateflowData();
        if (data != null) {
            for (State state : data.getStates()) {
                stateMap.put(state.getId(), state);
            }
            for (Transition trans : data.getTransitions()) {
                transitionMap.put(trans.getId(), trans);
            }
            // 设置默认状态为激活
            for (State state : stateMap.values()) {
                if (Boolean.TRUE.equals(state.getIsDefault()) || state.isInitial()) {
                    state.setActive(true);
                    activeStates.put(state.getId(), state);
                }
            }
        }

        this.initializing = false;
    }

    /**
     * 执行一次状态机步进。
     */
    public void step() {
        currentStep++;

        // 1. 处理事件队列中的事件
        while (!eventQueue.isEmpty()) {
            String event = eventQueue.poll();
            processEvent(event);
        }

        // 2. 执行所有激活状态的 during 动作
        for (State state : activeStates.values()) {
            state.onDuring(this);
        }
    }

    /**
     * 发送事件到事件队列。
     *
     * @param eventName 事件名称
     */
    public void sendEvent(String eventName) {
        if (eventName != null && !eventName.isEmpty()) {
            eventQueue.offer(eventName);
        }
    }

    /**
     * 检查事件队列中是否包含指定事件。
     *
     * @param eventName 事件名称
     * @return 如果包含则返回 true
     */
    public boolean hasEvent(String eventName) {
        return eventQueue.contains(eventName);
    }

    /**
     * 根据 ID 获取状态。
     *
     * @param stateId 状态 ID
     * @return 状态实体，不存在时返回 null
     */
    public State getStateById(String stateId) {
        return stateMap.get(stateId);
    }

    /**
     * 根据 ID 获取转移。
     *
     * @param transitionId 转移 ID
     * @return 转移实体，不存在时返回 null
     */
    public Transition getTransitionById(String transitionId) {
        return transitionMap.get(transitionId);
    }

    /**
     * 获取变量的当前值。
     *
     * @param variableName 变量名
     * @return 当前值，不存在时返回 null
     */
    public Object getVariableValue(String variableName) {
        return variableValues.get(variableName);
    }

    /**
     * 设置变量的当前值。
     *
     * @param variableName 变量名
     * @param value 新值
     */
    public void setVariableValue(String variableName, Object value) {
        variableValues.put(variableName, value);
    }

    // ===== 内部方法 =====

    private void processEvent(String event) {
        // 收集所有可能触发的转移，按优先级排序
        List<Transition> candidates = new ArrayList<>();
        for (Transition trans : transitionMap.values()) {
            if (trans.canFire(this)) {
                candidates.add(trans);
            }
        }

        // 按优先级排序（数字越小优先级越高）
        candidates.sort(Comparator.comparingInt(t -> t.getPriority() != null ? t.getPriority() : Integer.MAX_VALUE));

        // 触发第一个可用的转移
        for (Transition trans : candidates) {
            trans.fire(this);
            break; // 每次事件只触发一个转移
        }
    }
}
