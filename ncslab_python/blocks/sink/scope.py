from ..base import Block
from ...signal_utils import infer_signal_shape


class ScopeBlock(Block):
    def is_direct_feedthrough(self):
        return False

    def is_observer(self):
        return True

    def initialize(self):
        self.time_data = []
        self.sample_data = []
        self.num_ports = max(1, int(self._param("NumInputPorts", 1)))
        self.signal_width = self.num_ports
        self.signal_height = 1
        self.recording = False

    def compute_output(self, t, states):
        if self.recording:
            sample_height, sample_width, sample_flat = self._collect_sample()
            self._merge_sample_shape(sample_height, sample_width, sample_flat)
            self.time_data.append(t)
            self.sample_data.append(sample_flat)

    def get_scope_data(self, staircase=False):
        time_data, sample_data = self._build_export_samples(staircase=staircase)
        num_points = len(time_data)
        flat_data = []
        for sample in sample_data:
            flat_data.extend(sample)
        return {
            "type": 0,
            "name": self.block_name,
            "uuid": self.block_uuid,
            "path": self.block_path,
            "size": num_points,
            "length": num_points,
            "maxDataLength": num_points,
            "width": self.signal_width,
            "height": self.signal_height,
            "isFull": 0,
            "time": time_data,
            "data": flat_data,
        }

    def _build_export_samples(self, staircase=False):
        if not staircase or len(self.time_data) <= 1:
            return list(self.time_data), [list(sample) for sample in self.sample_data]

        staircase_time = [self.time_data[0]]
        staircase_samples = [list(self.sample_data[0])]

        for index in range(1, len(self.time_data)):
            current_time = self.time_data[index]
            previous_sample = list(self.sample_data[index - 1])
            current_sample = list(self.sample_data[index])
            staircase_time.append(current_time)
            staircase_samples.append(previous_sample)
            staircase_time.append(current_time)
            staircase_samples.append(current_sample)

        return staircase_time, staircase_samples

    def _collect_sample(self):
        if self.num_ports == 1:
            return infer_signal_shape(self.inputs.get(0, 0.0))

        flat = []
        only_scalars = True
        for index in range(self.num_ports):
            height, width, values = infer_signal_shape(self.inputs.get(index, 0.0))
            if height != 1 or width != 1:
                only_scalars = False
            flat.extend(values)

        if only_scalars:
            return 1, self.num_ports, flat if flat else [0.0]
        return 1, max(len(flat), 1), flat if flat else [0.0]

    def _merge_sample_shape(self, sample_height, sample_width, sample_flat):
        current_size = self.signal_height * self.signal_width
        sample_size = sample_height * sample_width

        if not self.sample_data:
            self.signal_height = sample_height
            self.signal_width = sample_width
            return

        if self.signal_height == sample_height and self.signal_width == sample_width:
            if len(sample_flat) < current_size:
                sample_flat.extend([0.0] * (current_size - len(sample_flat)))
            return

        target_size = max(current_size, sample_size)
        self.signal_height = 1
        self.signal_width = target_size

        for sample in self.sample_data:
            if len(sample) < target_size:
                sample.extend([0.0] * (target_size - len(sample)))

        if len(sample_flat) < target_size:
            sample_flat.extend([0.0] * (target_size - len(sample_flat)))
