#include <torch/torch.h>
#incldue <iostream>

/**
 * @brief A 1d convolutional neural network
 * 
 * @ref https://towardsdatascience.com/one-dimensional-cnn-for-human-behavior-classification-fb4371d03633
 */
class Conv1dNet : public torch::nn::Module {
public:
    Conv1dNet() {
        // Initialize the layers
        conv1 = register_module("conv1", torch::nn::Conv1d(torch::nn::Conv1dOptions(1, 32, 5)));
        conv2 = register_module("conv2", torch::nn::Conv1d(torch::nn::Conv1dOptions(32, 64, 5)));
        fc1 = register_module("fc1", torch::nn::Linear(64 * 5, 100));
        fc2 = register_module("fc2", torch::nn::Linear(100, 6));
    }

    // Implement the 1d CNN model
    torch::Tensor forward(torch::Tensor x) {
        x = torch::relu(conv1->forward(x));
        x = torch::relu(conv2->forward(x));
        x = torch::dropout(x, /*p=*/0.5);
        x = torch::max_pool1d(x, /*kernal_size=*/2);
        x = torch::flatten(x, /*start_dim=*/1);
        x = torch::relu(fc1->forward(x));
        x = fc2->forward(x);
        // x = torch::log_softmax(x, /*dim=*/1);
        return x;
    }

    // Define the layers
    torch::nn::Conv1d conv1{nullptr}, conv2{nullptr};
    torch::nn::Linear fc1{nullptr}, fc2{nullptr};
};