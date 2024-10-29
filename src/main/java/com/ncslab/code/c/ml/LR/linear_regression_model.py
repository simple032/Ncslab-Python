#!/usr/bin/env python
import torch
import torch.nn as nn
import numpy as np
import pandas as pd


class LinearRegression(nn.Module):
    def __init__(self,
                 input_features,
                 output_features,
                 loss_function_key='MSE',
                 device='cpu'):
        super(LinearRegression, self).__init__()
        self.input_features = input_features
        self.output_features = output_features
        self.loss_function_key = loss_function_key

        self.linear = nn.Linear(input_features, output_features)
        self.device = torch.device(device)
        self.loss_function = self._get_loss_function(self.loss_function_key)
        self.to(self.device)

        # print model info
        print(f"Model initialized with input features: {input_features}, output features: {output_features}")
        print(f"Using device: {self.device}")
        print(f"Loss function: {self.loss_function_key}")
        print(f"Model structure: {self.linear}")

    def forward(self, x):
        return self.linear(x)

    def train_by_file(self, filename, epochs, lr):
        data = pd.read_csv(filename)
        x = torch.tensor(data.iloc[:, :self.input_features].values, dtype=torch.float32)
        if x.dim() == 1:
            x = x.unsqueeze(1)

        y = torch.tensor(data.iloc[:, -self.output_features:].values, dtype=torch.float32)
        if y.dim() == 1:
            y = y.unsqueeze(1)
        print(f'shape of x: {x.shape}, y: {y.shape}')
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
            if ((epoch + 1) % 200 == 0):
                print(f"iter: [{epoch + 1}/{epochs}], loss: {loss.item()}")

    def predict(self, inputs):
        input_tensor = torch.tensor([inputs], dtype=torch.float32).to(self.device)
        with torch.no_grad():
            output = self(input_tensor)
        res = output.numpy()[0].tolist()
        # print(f'shape of numpy output: {output.numpy().shape}, type of res: {type(res)}, value of res: {res}')
        return res

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
                'loss_function_key': self.loss_function_key  # 损失函数名称
            }
            torch.save(model_info, path)  # 保存模型到文件
            print(f"Model saved successfully at {path}")
            # print model info
            print(
                f"Model initialized with input features: {self.input_features}, output features: {self.output_features}")
            print(f"Using device: {self.device}")
            print(f"Loss function: {self.loss_function_key}")
            print(f"Model structure: {self.linear}")
            print('state_dict: ', self.state_dict())
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

            # Loss function key
            self.loss_function_key = model_info['loss_function_key']

            # Update the model's linear layer to match the saved structure if necessary
            if input_features != self.linear.in_features or output_features != self.linear.out_features:
                self.linear = nn.Linear(input_features, output_features).to(self.device)

            # Load the saved state dictionary
            self.load_state_dict(model_info['state_dict'])

            # Update LossFunction:
            self.loss_function = self._get_loss_function(self.loss_function_key)

            print(f"Model loaded successfully from {path}")

            # print model info
            print(
                f"Model initialized with input features: {self.input_features}, output features: {self.output_features}")
            print(f"Using device: {self.device}")
            print(f"Loss function: {self.loss_function_key}")
            print(f"Model structure: {self.linear}")
            print('state_dict: ', self.state_dict())
            return 0
        except FileNotFoundError:
            print(f"Error: File not found at {path}")
            return -1
        except Exception as e:
            print(f"An error occurred while loading the model: {e}")
            return -1

# if __name__ == '__main__':
# model = LinearRegression(1,1,'MSE')
# model.load_model('lr.pth')
# model.train_by_file(r'D:\Sustech\M2PLab\prew\M2PLab\data\CCode\18\124\mlpdata.csv', 2000, 0.01)
# model.save_model('lr.pth')
# model.predict([[2.5]])
