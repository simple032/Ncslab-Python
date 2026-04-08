import json
import math
import sys
import unittest
from pathlib import Path

PYTHON_ROOT = Path(__file__).resolve().parents[1]
if str(PYTHON_ROOT) not in sys.path:
    sys.path.insert(0, str(PYTHON_ROOT))

from ncslab_python.model import SimulationModel
from ncslab_python.runner import run_simulation


class PythonCircuitGoldenTest(unittest.TestCase):
    def setUp(self):
        repo_root = Path(__file__).resolve().parents[5]
        self.fixtures_dir = repo_root / "src" / "test" / "resources" / "com" / "ncslab" / "websocket" / "python_circuit_basic"
        rules_path = self.fixtures_dir / "golden_rules.json"
        self.rules = json.loads(rules_path.read_text(encoding="utf-8"))
        self.abs_tol = float(self.rules.get("absTolerance", 1e-6))
        self.rel_tol = float(self.rules.get("relTolerance", 1e-6))

    def test_python_circuit_basic_fixtures_match_golden(self):
        for case in self.rules.get("cases", []):
            with self.subTest(fixture=case["fixture"]):
                fixture_path = self.fixtures_dir / case["fixture"]
                model_json = json.loads(fixture_path.read_text(encoding="utf-8"))
                model = SimulationModel(
                    model_json.get("config", {}),
                    model_json.get("blocks", []),
                    model_json.get("lines", []),
                    graph_data=model_json.get("graphData", {}),
                )
                run_result = run_simulation(model)
                self.assertIsNotNone(run_result, f"Simulation returned None for {case['fixture']}")

                scopes = model.get_scopes()
                expected_scopes = int(case.get("expectedScopes", 0))
                self.assertEqual(
                    expected_scopes,
                    len(scopes),
                    f"Scope count mismatch for {case['fixture']}",
                )

                if "expectedFinalValue" in case:
                    scope_index = int(case.get("scopeIndex", 0))
                    scope = scopes[scope_index]
                    data = scope.get("data", [])
                    self.assertTrue(data, f"No scope data for {case['fixture']}")
                    actual = float(data[-1])
                    expected = float(case["expectedFinalValue"])
                    delta = max(self.abs_tol, abs(expected) * self.rel_tol)
                    self.assertTrue(
                        math.isfinite(actual),
                        f"Non-finite final value for {case['fixture']}: {actual}",
                    )
                    self.assertAlmostEqual(
                        expected,
                        actual,
                        delta=delta,
                        msg=f"Final value mismatch for {case['fixture']}",
                    )


if __name__ == "__main__":
    unittest.main()
