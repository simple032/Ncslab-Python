from ..base import Block
from ...signal_utils import (
    elementwise_binary_op,
    elementwise_unary_op,
    infer_signal_shape,
    parse_signal_parameter,
    reshape_signal,
    signal_add,
    signal_multiply,
    scalar_of,
)


def _is_truthy(value):
    if isinstance(value, dict) and "value" in value:
        value = value["value"]
    if isinstance(value, bool):
        return value
    return str(value).strip().lower() in {"1", "true", "on", "yes"}


def _is_finite_scalar(value):
    return value == value and value not in (float("inf"), float("-inf"))


def _sanitize_signal(value, fallback=0.0):
    return elementwise_unary_op(
        value,
        lambda item: item if _is_finite_scalar(item) else fallback,
    )


def _clamp_signal(value, lower, upper):
    return elementwise_binary_op(
        elementwise_binary_op(value, upper, min),
        lower,
        max,
    )


def _reset_active(value):
    return abs(scalar_of(value)) > 0.0


def _resolve_state_shape(*values):
    target_height = 1
    target_width = 1
    for value in values:
        height, width, _ = infer_signal_shape(value)
        total = height * width
        target_total = target_height * target_width
        if total == 1:
            continue
        if target_total == 1:
            target_height = height
            target_width = width
            continue
        if (height, width) != (target_height, target_width):
            raise ValueError(
                f"Incompatible PID parameter shapes: {target_height}x{target_width} vs {height}x{width}"
            )
    return target_height, target_width


def _broadcast_to_shape(value, target_height, target_width):
    source_height, source_width, flat = infer_signal_shape(value)
    source_total = source_height * source_width
    target_total = target_height * target_width

    if source_total == target_total:
        return reshape_signal(target_height, target_width, flat)
    if source_total == 1:
        return reshape_signal(target_height, target_width, [flat[0]] * target_total)

    raise ValueError(
        f"Incompatible PID parameter shape: {source_height}x{source_width} vs {target_height}x{target_width}"
    )


class PIDControllerBlock(Block):
    """Continuous PID controller with integral and filtered derivative states."""

    def initialize(self):
        self.proportional_gain = parse_signal_parameter(self.param_values.get("P", 1.0))
        self.integral_gain = parse_signal_parameter(self.param_values.get("I", 1.0))
        self.derivative_gain = parse_signal_parameter(self.param_values.get("D", 0.0))
        self.filter_coefficient = parse_signal_parameter(self.param_values.get("N", 100.0))
        self.formulation_type = self._param_str("FormulationType", "Parallel")
        self.external_reset = self._param_str("ExternalReset", "none")
        self.limit_output = _is_truthy(self.param_values.get("LimitOutput", "off"))
        self.anti_windup_mode = self._param_str("AntiWindupMode", "none")
        self.back_calculation_gain = parse_signal_parameter(self.param_values.get("Kb", 1.0))

        initial_integrator = parse_signal_parameter(self.param_values.get("InitialConditionForIntegrator", 0.0))
        initial_filter = parse_signal_parameter(self.param_values.get("InitialConditionForFilter", 0.0))
        upper_limit = parse_signal_parameter(self.param_values.get("UpperSaturationLimit", "inf")) if self.limit_output else "inf"
        lower_limit = parse_signal_parameter(self.param_values.get("LowerSaturationLimit", "-inf")) if self.limit_output else "-inf"

        self.state_height, self.state_width = _resolve_state_shape(
            self.proportional_gain,
            self.integral_gain,
            self.derivative_gain,
            self.filter_coefficient,
            initial_integrator,
            initial_filter,
            self.back_calculation_gain if self.limit_output else 1.0,
            upper_limit,
            lower_limit,
        )

        self.initial_integrator = _broadcast_to_shape(initial_integrator, self.state_height, self.state_width)
        self.initial_filter = _broadcast_to_shape(initial_filter, self.state_height, self.state_width)
        self.upper_limit = _broadcast_to_shape(upper_limit, self.state_height, self.state_width)
        self.lower_limit = _broadcast_to_shape(lower_limit, self.state_height, self.state_width)

        _, _, self.initial_integrator_flat = infer_signal_shape(self.initial_integrator)
        _, _, self.initial_filter_flat = infer_signal_shape(self.initial_filter)
        self.state_size = len(self.initial_integrator_flat)
        self.num_states = self.state_size * 2
        self._effective_integral_state = self.initial_integrator
        self._effective_filter_state = self.initial_filter

    def get_initial_states(self):
        return self.initial_integrator_flat + self.initial_filter_flat

    def _state_signal(self, states, offset):
        return reshape_signal(
            self.state_height,
            self.state_width,
            list(states[offset:offset + self.state_size]),
        )

    def _apply_output_limits(self, value):
        if not self.limit_output:
            return value
        upper = _sanitize_signal(self.upper_limit, float("inf"))
        lower = _sanitize_signal(self.lower_limit, float("-inf"))
        return _clamp_signal(value, lower, upper)

    def _resolve_runtime_state(self, states):
        integral_state = _sanitize_signal(
            self._state_signal(states, self.state_offset),
            0.0,
        )
        filter_state = _sanitize_signal(
            self._state_signal(states, self.state_offset + self.state_size),
            0.0,
        )
        input_signal = _sanitize_signal(self.inputs.get(0, 0.0), 0.0)

        reset_enabled = self.external_reset.strip().lower() == "on"
        reset_signal = self.inputs.get(1, 0.0) if reset_enabled else 0.0
        if reset_enabled and _reset_active(reset_signal):
            integral_state = self.initial_integrator
            filter_state = self.initial_filter
            reset_applied = True
        else:
            reset_applied = False

        self._effective_integral_state = integral_state
        self._effective_filter_state = filter_state
        return input_signal, integral_state, filter_state, reset_applied

    def compute_output(self, t, states):
        del t

        if 0 not in self.inputs:
            self.outputs[0] = _sanitize_signal(self.initial_integrator, 0.0)
            return

        input_signal, integral_state, filter_state, reset_applied = self._resolve_runtime_state(states)
        proportional_term = signal_multiply(_sanitize_signal(self.proportional_gain, 0.0), input_signal)
        raw_output = signal_add(signal_add(proportional_term, integral_state), filter_state)

        if reset_applied:
            raw_output = signal_add(
                signal_add(proportional_term, self.initial_integrator),
                self.initial_filter,
            )

        self.outputs[0] = _sanitize_signal(self._apply_output_limits(raw_output), 0.0)

    def compute_derivative(self, t, states):
        del t

        if 0 not in self.inputs:
            return [0.0] * self.num_states

        input_signal, _, filter_state, reset_applied = self._resolve_runtime_state(states)

        if reset_applied:
            return [0.0] * self.num_states

        integral_derivative = signal_multiply(_sanitize_signal(self.integral_gain, 0.0), input_signal)
        derivative_target = signal_multiply(_sanitize_signal(self.derivative_gain, 0.0), input_signal)
        filter_derivative = signal_multiply(
            _sanitize_signal(self.filter_coefficient, 0.0),
            signal_add(derivative_target, signal_multiply(-1.0, filter_state)),
        )

        _, _, integral_flat = infer_signal_shape(integral_derivative)
        _, _, filter_flat = infer_signal_shape(filter_derivative)
        return integral_flat + filter_flat
