#include <torch/torch.h>


class transform {  
public:
    // Convert a matrix to a tensor
    static torch::Tensor matrix2tensor(Matrix matrix) {
        return torch::from_blob(matrix.data(), {matrix.rows(), matrix.cols()});
    }

    // Convert a tensor to a matrix
    static Matrix tensor2matrix(torch::Tensor tensor) {
        assert(tensor.dim() == 2);
        return Matrix(tensor.data_ptr<float>(), tensor.size(0), tensor.size(1));
    }
};