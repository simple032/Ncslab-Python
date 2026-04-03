from .derivative import DerivativeBlock
from .integrator import IntegratorBlock
from .pid_controller import PIDControllerBlock

try:
    from .state_space import StateSpaceBlock
except ModuleNotFoundError:
    StateSpaceBlock = None

try:
    from .transfer_fcn import TransferFcnBlock
except ModuleNotFoundError:
    TransferFcnBlock = None

__all__ = [
    "DerivativeBlock",
    "IntegratorBlock",
    "PIDControllerBlock",
]

if StateSpaceBlock is not None:
    __all__.append("StateSpaceBlock")
if TransferFcnBlock is not None:
    __all__.append("TransferFcnBlock")
