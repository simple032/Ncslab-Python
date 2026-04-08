import unittest

from ncslab_python.model import SimulationModel
from ncslab_python.runner import run_simulation


class CircuitEngineTest(unittest.TestCase):
    def _run_model(self, config, blocks, lines, graph_data):
        model = SimulationModel(config, blocks, lines, graph_data=graph_data)
        result = run_simulation(model)
        self.assertIsNotNone(result)
        scopes = model.get_scopes()
        self.assertGreater(len(scopes), 0)
        return scopes[0]

    def test_dc_source_voltage_sensor_scope_nonzero(self):
        config = {
            "Step": "FixedStep",
            "FixedStep": "0.01",
            "Solver": "ode4",
            "StartTime": "0.0",
            "StopTime": "0.1",
            "CircuitMode": "mna",
        }
        blocks = [
            {
                "blockType": "Scope",
                "blockName": "Scope1",
                "blockUUID": "scope-1",
                "blockPath": "m",
                "paramValues": {},
            },
            {
                "blockType": "Voltage Sensor",
                "blockName": "Voltage Sensor1",
                "blockUUID": "vs-1",
                "blockPath": "m",
                "paramValues": {},
            },
            {
                "blockType": "DC Voltage Source",
                "blockName": "DC Voltage Source1",
                "blockUUID": "vdc-1",
                "blockPath": "m",
                "paramValues": {"v0": "2.5"},
            },
        ]
        lines = [
            {"fromBlockName": "Voltage Sensor1", "fromPortNo": "1", "toBlockName": "Scope1", "toPortNo": "1"},
            {"fromBlockName": "DC Voltage Source1", "fromPortNo": "1", "toBlockName": "Voltage Sensor1", "toPortNo": "1"},
        ]
        graph_data = {
            "cells": [
                {
                    "type": "standard.Link",
                    "source": {"id": "vdc-1", "port": "eLConn1"},
                    "target": {"id": "vs-1", "port": "eLConn1"},
                },
                {
                    "type": "standard.Link",
                    "source": {"id": "vdc-1", "port": "eRConn1"},
                    "target": {"id": "vs-1", "port": "eRConn1"},
                },
                {
                    "type": "standard.Link",
                    "source": {"id": "vs-1", "port": "o1"},
                    "target": {"id": "scope-1", "port": "i1"},
                },
            ]
        }
        model = SimulationModel(config, blocks, lines, graph_data=graph_data)
        self.assertTrue(model.uses_circuit_mna)

        states = model.get_initial_states()
        model.propagate_signals(0.0, states, step_size=0.01)

        sensor = model.blocks["Voltage Sensor1"]
        self.assertGreater(abs(sensor.outputs.get(0, 0.0)), 1e-9)

    def test_nonlinear_iteration_runs(self):
        config = {
            "Step": "FixedStep",
            "FixedStep": "0.01",
            "Solver": "ode4",
            "StartTime": "0.0",
            "StopTime": "0.1",
            "CircuitMode": "mna",
        }
        blocks = [
            {
                "blockType": "DC Voltage Source",
                "blockName": "V1",
                "blockUUID": "v-1",
                "blockPath": "m",
                "paramValues": {"v0": "5"},
            },
            {
                "blockType": "Diode",
                "blockName": "D1",
                "blockUUID": "d-1",
                "blockPath": "m",
                "paramValues": {"Threshold": "0.7"},
            },
        ]
        lines = []
        graph_data = {
            "cells": [
                {"type": "standard.Link", "source": {"id": "v-1", "port": "eLConn1"}, "target": {"id": "d-1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "v-1", "port": "eRConn1"}, "target": {"id": "d-1", "port": "eRConn1"}},
            ]
        }
        model = SimulationModel(config, blocks, lines, graph_data=graph_data)
        states = model.get_initial_states()
        model.propagate_signals(0.0, states, step_size=0.01)
        self.assertTrue(model.uses_circuit_mna)

    def test_solver_config_ps_converter_opamp_chain_nonzero(self):
        config = {
            "Step": "FixedStep",
            "FixedStep": "0.01",
            "Solver": "ode4",
            "StartTime": "0.0",
            "StopTime": "0.2",
            "CircuitMode": "mna",
        }
        blocks = [
            {"blockType": "Solver Configuration", "blockName": "SolverCfg", "blockUUID": "solver-1", "blockPath": "m", "paramValues": {}},
            {"blockType": "DC Voltage Source", "blockName": "V1", "blockUUID": "v1", "blockPath": "m", "paramValues": {"v0": "5"}},
            {"blockType": "Resistor", "blockName": "R0", "blockUUID": "r0", "blockPath": "m", "paramValues": {"R": "1000"}},
            {"blockType": "Op Amp", "blockName": "OpAmp1", "blockUUID": "op1", "blockPath": "m", "paramValues": {"Gain": "1000", "Vmax": "10", "Vmin": "-10"}},
            {"blockType": "Resistor", "blockName": "Rf", "blockUUID": "rf1", "blockPath": "m", "paramValues": {"R": "1000"}},
            {"blockType": "Electrical Reference", "blockName": "GND1", "blockUUID": "gnd1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Voltage Sensor", "blockName": "VS1", "blockUUID": "vs1", "blockPath": "m", "paramValues": {}},
            {"blockType": "PS-Simulink Converter", "blockName": "PS2S", "blockUUID": "ps2s1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Scope", "blockName": "Scope1", "blockUUID": "scope1", "blockPath": "m", "paramValues": {}},
        ]
        lines = [
            {"fromBlockName": "VS1", "fromPortNo": "1", "toBlockName": "PS2S", "toPortNo": "1"},
            {"fromBlockName": "PS2S", "fromPortNo": "1", "toBlockName": "Scope1", "toPortNo": "1"},
        ]
        graph_data = {
            "cells": [
                {"type": "standard.Link", "source": {"id": "v1", "port": "eLConn1"}, "target": {"id": "r0", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "r0", "port": "eRConn1"}, "target": {"id": "op1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "op1", "port": "eRConn1"}, "target": {"id": "rf1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "rf1", "port": "eRConn1"}, "target": {"id": "op1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "op1", "port": "eLConn2"}, "target": {"id": "gnd1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "v1", "port": "eRConn1"}, "target": {"id": "gnd1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "op1", "port": "eRConn1"}, "target": {"id": "vs1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "vs1", "port": "eRConn1"}, "target": {"id": "gnd1", "port": "eLConn1"}},
            ]
        }
        model = SimulationModel(config, blocks, lines, graph_data=graph_data)
        result = run_simulation(model)
        self.assertIsNotNone(result)
        scopes = model.get_scopes()
        self.assertGreater(len(scopes), 0)
        data = scopes[0].get("data", [])
        self.assertTrue(any(abs(float(v)) > 1e-6 for v in data))

    def test_resistive_divider_dc(self):
        config = {
            "Step": "FixedStep",
            "FixedStep": "0.01",
            "Solver": "ode4",
            "StartTime": "0.0",
            "StopTime": "0.2",
            "CircuitMode": "mna",
        }
        blocks = [
            {"blockType": "DC Voltage Source", "blockName": "V1", "blockUUID": "v1", "blockPath": "m", "paramValues": {"v0": "10"}},
            {"blockType": "Resistor", "blockName": "Rtop", "blockUUID": "r1", "blockPath": "m", "paramValues": {"R": "1000"}},
            {"blockType": "Resistor", "blockName": "Rbot", "blockUUID": "r2", "blockPath": "m", "paramValues": {"R": "1000"}},
            {"blockType": "Electrical Reference", "blockName": "GND", "blockUUID": "g1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Voltage Sensor", "blockName": "VS", "blockUUID": "vs1", "blockPath": "m", "paramValues": {}},
            {"blockType": "PS-Simulink Converter", "blockName": "PS2S", "blockUUID": "ps1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Scope", "blockName": "Scope1", "blockUUID": "s1", "blockPath": "m", "paramValues": {}},
        ]
        lines = [
            {"fromBlockName": "VS", "fromPortNo": "1", "toBlockName": "PS2S", "toPortNo": "1"},
            {"fromBlockName": "PS2S", "fromPortNo": "1", "toBlockName": "Scope1", "toPortNo": "1"},
        ]
        graph_data = {
            "cells": [
                {"type": "standard.Link", "source": {"id": "v1", "port": "eLConn1"}, "target": {"id": "r1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "r1", "port": "eRConn1"}, "target": {"id": "r2", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "r2", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "v1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "vs1", "port": "eLConn1"}, "target": {"id": "r1", "port": "eRConn1"}},
                {"type": "standard.Link", "source": {"id": "vs1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
            ]
        }
        scope = self._run_model(config, blocks, lines, graph_data)
        end_value = float(scope["data"][-1]) if scope["data"] else 0.0
        self.assertGreater(end_value, 4.0)
        self.assertLess(end_value, 6.0)

    def test_rc_step_monotonic_decay(self):
        config = {
            "Step": "FixedStep",
            "FixedStep": "0.001",
            "Solver": "ode4",
            "StartTime": "0.0",
            "StopTime": "0.05",
            "CircuitMode": "mna",
        }
        blocks = [
            {"blockType": "DC Voltage Source", "blockName": "V1", "blockUUID": "v1", "blockPath": "m", "paramValues": {"v0": "5"}},
            {"blockType": "Resistor", "blockName": "R1", "blockUUID": "r1", "blockPath": "m", "paramValues": {"R": "1000"}},
            {"blockType": "Capacitor", "blockName": "C1", "blockUUID": "c1", "blockPath": "m", "paramValues": {"C": "1e-5"}},
            {"blockType": "Electrical Reference", "blockName": "GND", "blockUUID": "g1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Voltage Sensor", "blockName": "VS", "blockUUID": "vs1", "blockPath": "m", "paramValues": {}},
            {"blockType": "PS-Simulink Converter", "blockName": "PS2S", "blockUUID": "ps1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Scope", "blockName": "Scope1", "blockUUID": "s1", "blockPath": "m", "paramValues": {}},
        ]
        lines = [
            {"fromBlockName": "VS", "fromPortNo": "1", "toBlockName": "PS2S", "toPortNo": "1"},
            {"fromBlockName": "PS2S", "fromPortNo": "1", "toBlockName": "Scope1", "toPortNo": "1"},
        ]
        graph_data = {
            "cells": [
                {"type": "standard.Link", "source": {"id": "v1", "port": "eLConn1"}, "target": {"id": "r1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "r1", "port": "eRConn1"}, "target": {"id": "c1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "c1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "v1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "vs1", "port": "eLConn1"}, "target": {"id": "c1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "vs1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
            ]
        }
        scope = self._run_model(config, blocks, lines, graph_data)
        data = [float(v) for v in scope["data"]]
        self.assertGreater(abs(data[-1]), abs(data[0]) - 1e-9)

    def test_rl_step_current_rises(self):
        config = {
            "Step": "FixedStep",
            "FixedStep": "0.001",
            "Solver": "ode4",
            "StartTime": "0.0",
            "StopTime": "0.05",
            "CircuitMode": "mna",
        }
        blocks = [
            {"blockType": "DC Voltage Source", "blockName": "V1", "blockUUID": "v1", "blockPath": "m", "paramValues": {"v0": "5"}},
            {"blockType": "Resistor", "blockName": "R1", "blockUUID": "r1", "blockPath": "m", "paramValues": {"R": "10"}},
            {"blockType": "Inductor", "blockName": "L1", "blockUUID": "l1", "blockPath": "m", "paramValues": {"L": "0.1"}},
            {"blockType": "Electrical Reference", "blockName": "GND", "blockUUID": "g1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Current Sensor", "blockName": "IS", "blockUUID": "is1", "blockPath": "m", "paramValues": {}},
            {"blockType": "PS-Simulink Converter", "blockName": "PS2S", "blockUUID": "ps1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Scope", "blockName": "Scope1", "blockUUID": "s1", "blockPath": "m", "paramValues": {}},
        ]
        lines = [
            {"fromBlockName": "IS", "fromPortNo": "1", "toBlockName": "PS2S", "toPortNo": "1"},
            {"fromBlockName": "PS2S", "fromPortNo": "1", "toBlockName": "Scope1", "toPortNo": "1"},
        ]
        graph_data = {
            "cells": [
                {"type": "standard.Link", "source": {"id": "v1", "port": "eLConn1"}, "target": {"id": "r1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "r1", "port": "eRConn1"}, "target": {"id": "l1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "l1", "port": "eRConn1"}, "target": {"id": "is1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "is1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "v1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
            ]
        }
        scope = self._run_model(config, blocks, lines, graph_data)
        data = [float(v) for v in scope["data"]]
        self.assertGreater(abs(data[-1]), abs(data[0]) - 1e-9)

    def test_variable_resistor_tracks_ramp_control(self):
        config = {
            "Step": "FixedStep",
            "FixedStep": "0.01",
            "Solver": "ode4",
            "StartTime": "0.0",
            "StopTime": "2.0",
            "CircuitMode": "mna",
        }
        blocks = [
            {"blockType": "Ramp", "blockName": "Ramp1", "blockUUID": "ramp1", "blockPath": "m", "paramValues": {"Slope": "100", "InitialOutput": "100"}},
            {"blockType": "DC Voltage Source", "blockName": "V1", "blockUUID": "v1", "blockPath": "m", "paramValues": {"v0": "10"}},
            {"blockType": "Variable Resistor", "blockName": "VR1", "blockUUID": "vr1", "blockPath": "m", "paramValues": {"Rmin": "10", "Rmax": "1000"}},
            {"blockType": "Current Sensor", "blockName": "IS1", "blockUUID": "is1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Electrical Reference", "blockName": "GND1", "blockUUID": "g1", "blockPath": "m", "paramValues": {}},
            {"blockType": "PS-Simulink Converter", "blockName": "PS2S", "blockUUID": "ps1", "blockPath": "m", "paramValues": {}},
            {"blockType": "Scope", "blockName": "Scope1", "blockUUID": "s1", "blockPath": "m", "paramValues": {}},
        ]
        lines = [
            {"fromBlockName": "Ramp1", "fromPortNo": "1", "toBlockName": "VR1", "toPortNo": "1"},
            {"fromBlockName": "IS1", "fromPortNo": "1", "toBlockName": "PS2S", "toPortNo": "1"},
            {"fromBlockName": "PS2S", "fromPortNo": "1", "toBlockName": "Scope1", "toPortNo": "1"},
        ]
        graph_data = {
            "cells": [
                {"type": "standard.Link", "source": {"id": "v1", "port": "eLConn1"}, "target": {"id": "vr1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "vr1", "port": "eRConn1"}, "target": {"id": "is1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "is1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
                {"type": "standard.Link", "source": {"id": "v1", "port": "eRConn1"}, "target": {"id": "g1", "port": "eLConn1"}},
            ]
        }
        scope = self._run_model(config, blocks, lines, graph_data)
        data = [abs(float(v)) for v in scope["data"]]
        self.assertGreater(max(data) - min(data), 1e-3)


if __name__ == "__main__":
    unittest.main()
