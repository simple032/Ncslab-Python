#include <string>
#include "torch/torch.h"
#include <unordered_map>

namespace ncsml{
    class MLModel : public torch::nn::Module{
    public:

        virtual void train(torch::Tensor&x, torch::Tensor&y, size_t epochs, double lr) = 0;

        virtual torch::Tensor predict(torch::Tensor&x) = 0;

        virtual double predict(std::vector<double> inputs)=0;

        void setLoss(std::string lossname){ 
            this->LOSS_KEY = lossname;
        }

        void setActivation(std::string activationname){
            this->ACTIVATION_KEY = activationname;
        }

        virtual ~MLModel(){
            // delete this->criterion;

        }


    protected:

        
        torch::nn::AnyModule criterion;
        std::string LOSS_KEY;
        std::string ACTIVATION_KEY;

        torch::Tensor tanh(torch::Tensor x) {
            return torch::tanh(x);
        }

        torch::Tensor d_tanh(torch::Tensor x) {
            return 1 - torch::pow(torch::tanh(x), 2);
        }

        // softmax function
        torch::Tensor softmax(torch::Tensor x) {
            return torch::softmax(x, 1);
        }

        torch::Tensor mse_loss(torch::Tensor&prediction, torch::Tensor&y) {
            return torch::mse_loss(prediction, y);
        }

        torch::Tensor cross_entropy_loss(torch::Tensor&prediction, torch::Tensor&y) {
            return torch::nll_loss(prediction, y);
        }

        torch::Tensor binary_cross_entropy_loss(torch::Tensor&prediction, torch::Tensor&y) {
            return torch::binary_cross_entropy(prediction, y);
        }

        torch::Tensor getLoss(std::string lossName, torch::Tensor prediction, torch::Tensor y){
            if(lossName == "MSE"){
                return mse_loss(prediction, y);
                
            }else if(lossName == "MSE"){
                return cross_entropy_loss(prediction, y);
            }
            return binary_cross_entropy_loss(prediction, y);
        }
        

        void setLossModel() {
        if (LOSS_KEY == "MSE") {
                // Set the criterion as a shared pointer wrapped inside an AnyModule
                criterion = torch::nn::AnyModule(torch::nn::MSELoss());

            } else if (LOSS_KEY == "CROSS") {
                criterion = torch::nn::AnyModule(torch::nn::CrossEntropyLoss());
            } else {
                criterion = torch::nn::AnyModule(torch::nn::MSELoss()); // 默认使用 MSE
            }
        }


        
    };
};



/**************** FOR TEST *************/

torch::Tensor csv_to_tensor(const std::string& filename, torch::Tensor& y, torch::Tensor& x) {
    std::ifstream file(filename);
    if (!file.is_open()) {
        std::cerr << "Error opening file: " << filename << std::endl;
        exit(1);
    }

    std::string line, word;
    std::vector<std::vector<float>> data;

    std::getline(file, line);

    while (std::getline(file, line)) {
        std::stringstream s(line);
        std::vector<float> row;
        while (std::getline(s, word, ',')) {
            row.push_back(std::stof(word));
        }
        data.push_back(row);
    }

    size_t rows = data.size();
    size_t cols = rows ? data[0].size() : 0;
    torch::Tensor tensor = torch::empty({ static_cast<long>(rows), static_cast<long>(cols) });
    for (size_t i = 0; i < rows; i++) {
        for (size_t j = 0; j < cols; j++) {
            tensor[i][j] = data[i][j];
        }
    }

    // Separate Y and X data 
    y = tensor.index({torch::indexing::Slice(), torch::indexing::Slice(3, 4)});
    x = tensor.index({torch::indexing::Slice(), torch::indexing::Slice(0, 3)});

    return tensor;
}

class LinearRegression : public ncsml::MLModel
{
public:
    //Linear Regression
    LinearRegression(int inputFeatures, int outputFeatures = 1) {
        // create linear regression layer
        this->linear = register_module("linear", torch::nn::Linear(torch::nn::LinearOptions(inputFeatures, outputFeatures)));
    }
 
    // forward
    torch::Tensor forward(torch::Tensor x) {
        x = linear(x);
        return x;
    }

    /*
    * Train the model. 
    * @param
    * torch::Tensor&x: training input x
    * totch::Tensor&y: training label y
    * int epochs: iterarion
    * double lr: learning rate 
    */
    void train(torch::Tensor&x, torch::Tensor&y, size_t epochs, double lr = 0.001) override{
        // record the gridient. 
        // follow the following way to define it will be okey.
        x.set_requires_grad(1);
        y.set_requires_grad(1);
        // define optimizer
        torch::optim::SGD optimizer(this->parameters(), lr);


        for(size_t i = 0; i < epochs; ++i){
            // get output
            torch::Tensor out = this->forward(x);
            //get loss situation
            torch::Tensor loss = getLoss("MSE", out, y);
            // reset optimizer
            optimizer.zero_grad();
            // backward propagation
            loss.backward();
            // update optimizer, this will update the parameter stored in our model.
            optimizer.step();

            // if(epochs % 100 == 0)
                // //std::cout << "Epochs: "<< i+1 << "-th loss is: " << loss.item<float>() << std::endl;
        }

    }

    // predict the model.
    torch::Tensor predict(torch::Tensor&x) override{
        return this->forward(x);
    }
    
    double predict(std::vector<double> inputs) {
        // transport to tensor
        torch::Tensor input_tensor = torch::tensor(inputs);

        //std::cout << "input tensor: "<< input_tensor << std::endl;
        //std::cout << "input size: "<< input_tensor.sizes() << std::endl;
        torch::Tensor output_tensor = predict(input_tensor);
        //std::cout << "output tensor: "<< output_tensor << std::endl;
        //std::cout << "output size: "<< output_tensor.sizes() << std::endl;

        // to double
        return output_tensor.item<double>();
    }
    
private:
    torch::nn::Linear linear{ nullptr };
};

ncsml::MLModel *machineLearningModel;

double getResult233(double d1, double d2, double d3){
    LinearRegression regressior(3,1);
    std::string filename = "/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv"; // Please change this path to your path to .csv file
    torch::Tensor y, x;
    torch::Tensor tensor = csv_to_tensor(filename, y, x);
    x.set_requires_grad(true);
    y.set_requires_grad(true);

    regressior.setLoss("MSE");
    regressior.train(x, y, 2000, 0.01);

    auto predictions = regressior.predict(x);
    std::vector<double> input = { d1,  d2, d3};
    return regressior.predict(input);
}

void initModel(){
    machineLearningModel = new LinearRegression(3,1);
}

void train_model(ncsml::MLModel*m){
    std::string filename = "/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv"; // Please change this path to your path to .csv file
    torch::Tensor y, x;
    torch::Tensor tensor = csv_to_tensor(filename, y, x);
    x.set_requires_grad(true);
    y.set_requires_grad(true);
    
    // 生成长度为50的x张量
    // int len = 25;
    // torch::Tensor x_1 = torch::rand({len}); // 生成50个x_1的随机值
    // torch::Tensor x_2 = torch::rand({len}); // 生成50个x_2的随机值
    // torch::Tensor x_3 = torch::rand({len}); // 生成50个x_3的随机值

    // 将x_1, x_2, x_3堆叠成一个50x3的张量
    // torch::Tensor x = torch::stack({x_1, x_2, x_3}, /*dim=*/1);
    // std::cout << "shape of x: "<< x.sizes() << std::endl;
    // 生成y张量
    // torch::Tensor y = x_1.pow(2) + x_1 + x_2 + x_1 * x_3;

    // y = y.reshape({len, 1});
    // std::cout << "x: " << x << std::endl;
    // std::cout << "shape of y: "<< y.sizes() << std::endl;
    // std::cout << "y: " << y << std::endl;
    x.set_requires_grad(true);
    y.set_requires_grad(true);

    machineLearningModel->setLoss("MSE");
    machineLearningModel->train(x, y, 200, 0.01);

}

double predict(std::vector<double> inputs){
    return machineLearningModel->predict(inputs);
}

double getResult(ncsml::MLModel*m, double d1, double d2, double d3){
    std::vector<double> input = { d1,  d2*d2, d3};
    return machineLearningModel->predict(input);
}

double getTestResult(ncsml::MLModel*m, double d1){
    std::vector<double> input = {d1};
    return machineLearningModel->predict(input);
}

// int main(){
//     // torch::Tensor x = torch::unsqueeze(torch::linspace(0,19,20),1);
//     // torch::Tensor y = 5*x+torch::randint(1,20,x.sizes());

//     initModel();
//     train_model(machineLearningModel);

//     std::cout << "result: "<< getResult(machineLearningModel, 4.4600, 1.0980, 1.7620) << std::endl;

// }

// int main233(){
//     // LinearRegression regressior(1);
//     // torch::Tensor x = torch::unsqueeze(torch::linspace(0,19,20),1);
//     // torch::Tensor y = 5*x+torch::randint(1,20,x.sizes());
//     // initModel();
//     LinearRegression regressior(3,1);
//     std::string filename = "/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv"; // Please change this path to your path to .csv file
//     torch::Tensor y, x;
//     torch::Tensor tensor = csv_to_tensor(filename, y, x);
//     x.set_requires_grad(true);
//     y.set_requires_grad(true);
//     //std::cout << x << std::endl;
//     //std::cout << "y:\n"<<y<<std::endl;

//     regressior.setLoss("MSE");
//     regressior.train(x, y, 5000, 0.01);

//     //std::cout << "x shape: " << x.sizes() << std::endl;
//     //std::cout << "y shape: " << y.sizes() << std::endl;

//     auto predictions = regressior.predict(x);
//     //std::cout << "original y: \n"<< y << std::endl;
//     //std::cout << "predict y: \n" << predictions << std::endl;
//     std::vector<double> input = { 4.4600 , 1.0980 , 1.7620};
//     //std::cout<<"Test results:\n" << getResult(4.4600 , 1.0980 , 1.7620) << std::endl;

//     initModel();
//     train_model();
//     std::vector<double> inputs = {4.4600, 1.0980, 1.7620};
//     //std::cout << "Predict results: " << predict(inputs) << std::endl;
//     return 0;
// }

