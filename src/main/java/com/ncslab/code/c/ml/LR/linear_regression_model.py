#!/usr/bin/env python
import torch
import torch.nn as nn
import numpy as np
import pandas as pd

class LinearRegression(nn.Module):
    def __init__(self, input_features, output_features, loss_function = 'CE', device='cpu'):
        super(LinearRegression, self).__init__()
        self.linear = nn.Linear(input_features, output_features)
        self.device = torch.device(device)
        self.loss_function = self._get_loss_function(loss_function)
        self.to(self.device)

    def forward(self, x):
        return self.linear(x)

    def train_by_file(self, filename, epochs, lr):
        data = pd.read_csv(filename)
        x = torch.tensor(data.iloc[:, :-1].values, dtype=torch.float32)
        y = torch.tensor(data.iloc[:, -1].values, dtype=torch.float32)
        y = y.view(y.shape[0], 1)
        self.fit(x, y, epochs, lr)

    def fit(self, x, y, epochs, lr):
        x = x.to(self.device)
        y = y.to(self.device)
        criterion = self.loss_function
        optimizer = torch.optim.SGD(self.parameters(), lr=lr)
        for epoch in range(epochs):
            optimizer.zero_grad()
            outputs = self(x)
            loss = criterion(outputs, y)
            loss.backward()
            optimizer.step()

    def predict(self, inputs):
        input_tensor = torch.tensor([inputs], dtype=torch.float32).to(self.device)
        with torch.no_grad():
            output = self(input_tensor)
        return output.numpy()[0].tolist()

    def _get_loss_function(self, loss_function):
        if loss_function == 'CE':
            return nn.CrossEntropyLoss()
        elif loss_function == 'MSE':
            return nn.MSELoss()
        else:
            return nn.MSELoss()

    def save_model(self, path):
        try:
            model_info = {
                'state_dict': self.state_dict(),  # 模型权重
                'input_features': self.linear.in_features,  # 输入特征
                'output_features': self.linear.out_features,  # 输出特征
                # 'activation_function': self.activation_function,  # 激活函数
                'loss_function': self.loss_function.__class__.__name__  # 损失函数名称
            }
            torch.save(model_info, path)  # 保存模型到文件
            print(f"Model saved successfully at {path}")
        except FileNotFoundError:
            print(f"Error: File not found at {path}")
            return -1
        except FileExistsError:
            print(f"Error: File already exists at {path}")
            return 1
        except KeyboardInterrupt:
            print("Process interrupted")
            return -1
        return 0

    def load_model(self, path):
        try:
            # Load the saved model information
            print('path load from: ', path)
            model_info = torch.load(path, map_location=self.device, weights_only=True)

            # Extract input and output feature sizes from the saved model
            input_features = model_info['input_features']
            output_features = model_info['output_features']

            # Update the model's linear layer to match the saved structure if necessary
            if input_features != self.linear.in_features or output_features != self.linear.out_features:
                self.linear = nn.Linear(input_features, output_features).to(self.device)

            # Load the saved state dictionary
            self.load_state_dict(model_info['state_dict'])

            # Set the loss function based on the saved model info
            loss_function_name = model_info.get('loss_function', 'MSE')
            self.loss_function = self._get_loss_function(loss_function_name)

            print(f"Model loaded successfully from {path}")
            return 0
        except FileNotFoundError:
            print(f"Error: File not found at {path}")
            return -1
        except Exception as e:
            print(f"An error occurred while loading the model: {e}")
            return -1


#################### PREVIOUS CODES #####################
# model = None

# def init_model(input_feature=3, output_feature=1):
#     global model
#     model = LinearRegression(input_feature, output_feature)
#     return model

# def train_model(filename, epochs, lr):
#     data = pd.read_csv(filename)
#     x = torch.tensor(data.iloc[:, :-1].values, dtype=torch.float32)
#     y = torch.tensor(data.iloc[:, -1].values, dtype=torch.float32)
#     y = y.view(y.shape[0], 1)
#     criterion = nn.MSELoss()
#     optimizer = torch.optim.SGD(model.parameters(), lr=lr)
#     for epoch in range(epochs):
#         optimizer.zero_grad()
#         outputs = model(x)
#         loss = criterion(outputs, y)
#         loss.backward()
#         optimizer.step()

# def predict(inputs):
#     input_tensor = torch.tensor([inputs], dtype=torch.float32)
#     with torch.no_grad():
#         output = model(input_tensor)
#     return output.numpy()[0].tolist()

# if __name__ == '__main__':
#     my_model = LinearRegression(3, 1)
#     print(my_model.predict([1, 2, 3]))
#     init_model()
#     print(predict([1, 2, 3]))
