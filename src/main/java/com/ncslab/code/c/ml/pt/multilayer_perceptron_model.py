#!/usr/bin/env python
import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
import pandas as pd
import numpy as np


class MLP(nn.Module):
    def __init__(self,
                 input_size,
                 output_size,
                 hidden_layers,
                 loss_function_key="MSE",
                 activation_key="relu",
                 device='cpu'):
        super(MLP, self).__init__()
        # print("start init")
        self.input_size = input_size
        self.output_size = output_size
        self.hidden_layers = hidden_layers
        self.loss_function_key = loss_function_key
        self.activation_key = activation_key
        self.device = torch.device(device)

        layers = []
        in_features = input_size

        self.hidden_activate = self._get_activation_function(self.activation_key)

        for hidden_size in hidden_layers:
            layers.append(nn.Linear(in_features, hidden_size))
            if activation_key != 'linear':
                layers.append(self.hidden_activate)
            in_features = hidden_size

        layers.append(nn.Linear(in_features, output_size))
        self.model = nn.Sequential(*layers)

        self.loss_function = self._get_loss_function(self.loss_function_key)
        self.to(self.device)
        # print("end init")
        if isinstance(self.loss_function, nn.CrossEntropyLoss):
            print("CrossEntropyLoss")
        elif isinstance(self.loss_function, nn.MSELoss):
            print("MSELoss")
        print('Activation function: ', self.hidden_activate)

        # Print model info
        print(f"Model initialized with input size: {input_size}, output size: {output_size}")
        print(f"Hidden layers: {hidden_layers}")
        print(f"Using device: {self.device}")
        print(f"Loss function: {self.loss_function_key}")
        print(f"Model structure: {self.model}")

        # Print model weights
        for name, param in self.model.named_parameters():
            if param.requires_grad:
                print(f"Layer: {name} | Size: {param.size()} | Values : {param[:2]} \n")
        print('========== end initialization! ==========')

    def set_loss_function(self, loss_function):
        self.loss_function = self._get_loss_function(loss_function)

    def forward(self, x):
        x = x.to(self.device)
        return self.model(x)

    def fit(self, X_train, y_train, epochs=100, lr=0.01):
        X_train = X_train.to(self.device)
        y_train = y_train.to(self.device)

        # Loss and optimizer
        # criterion = nn.CrossEntropyLoss()
        optimizer = optim.SGD(self.parameters(), lr=lr)

        for epoch in range(epochs):
            self.train()

            # Forward pass
            outputs = self(X_train)
            loss = self.loss_function(outputs, y_train)

            # Backward pass and optimization
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()
            if ((epoch + 1) % 200 == 0):
                print(f"iter: [{epoch + 1}/{epochs}], loss: {loss.item()}")
        # print("end fit")

    def predict(self, inputs):
        # print('start predict')
        input_tensor = torch.tensor([inputs], dtype=torch.float32).to(self.device)
        with torch.no_grad():
            output = self(input_tensor)
        return output.numpy()[0].tolist()

    def train_by_file(self, filename, epochs, lr):
        data = pd.read_csv(filename)
        x = torch.tensor(data.iloc[:, :self.input_size].values, dtype=torch.float32)
        if x.dim() == 1:
            x = x.unsqueeze(1)

        y = torch.tensor(data.iloc[:, -self.output_size:].values, dtype=torch.float32)
        if y.dim() == 1:
            y = y.unsqueeze(1)
        self.fit(x, y, epochs, lr)

    def _get_loss_function(self, loss_function):
        if (loss_function == 'CE'):
            return nn.CrossEntropyLoss()
        elif (loss_function == 'MSE'):
            return nn.MSELoss()
        else:
            return nn.CrossEntropyLoss()

    def _check_hidden(self, target_list) -> bool:
        origin_len = len(self.hidden_layers)
        if (len(target_list) != len(self.hidden_layers)):
            return False
        for i in range(origin_len):
            if target_list[i] != self.hidden_layers[i]:
                return False
        return True

    def _get_activation_function(self, activation_function):
        if (activation_function == 'elu'):
            return nn.ELU()
        elif (activation_function == 'sigmoid'):
            return nn.Sigmoid()
        elif (activation_function == 'tanh'):
            return nn.Tanh()
        # elif (activation_function == 'lrelu'):
        #     return nn.LeakyReLU()
        else:
            return nn.ELU()

    def save_model(self, path):
        try:
            model_info = {
                'state_dict': self.state_dict(),  # 保存模型的权重
                'input_size': self.input_size,  # 输入特征数
                'output_size': self.output_size,  # 输出特征数
                'hidden_layers': self.hidden_layers,
                'loss_function_key': self.loss_function_key,  # 保存损失函数的名称
                'activation_key': self.activation_key  # 保存激活函数的名称
            }
            # 保存模型到指定的路径
            torch.save(model_info, path)
            print(f"Model saved successfully at {path}")

            # 打印保存模型的信息
            print("Model information:")
            print(f"Input size: {model_info['input_size']}")
            print(f"Output size: {model_info['output_size']}")
            print(f"Hidden layers: {model_info['hidden_layers']}")
            print(f"Loss function: {model_info['loss_function_key']}")
            print(f"Activation function: {model_info['activation_key']}")

            # 打印模型权重信息
            print("Model weights:")
            for name, param in self.model.named_parameters():
                if param.requires_grad:
                    print(f"Layer: {name} | Size: {param.size()} | Values : {param[:2]} \n")
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
            model_info = torch.load(path, map_location=self.device, weights_only=True)

            # Extract input and output feature sizes from the saved model
            input_size = model_info['input_size']
            output_size = model_info['output_size']
            hidden_layers = model_info['hidden_layers']

            # Extract loss function and activation function names
            loss_function_key = model_info['loss_function_key']
            activation_key = model_info['activation_key']

            # Rebuild the model structure with the saved parameters if necessary
            if input_size != self.input_size or output_size != self.output_size or (
            not self._check_hidden(hidden_layers)):
                self.__init__(input_size,
                              output_size,
                              hidden_layers,
                              loss_function_key=loss_function_key,
                              activation_key=activation_key,
                              device=self.device)

            # Load the saved state dictionary (weights)
            self.load_state_dict(model_info['state_dict'])

            print(f"Model loaded successfully from {path}")

            # print model info
            print("Loaded model information:")
            print(f"Input size: {input_size}")
            print(f"Output size: {output_size}")
            print(f"Hidden layers: {hidden_layers}")
            print(f"Loss function: {loss_function_key}")
            print(f"Activation function: {activation_key}")

            # weights
            print("Model weights:")
            for name, param in self.model.named_parameters():
                if param.requires_grad:
                    print(f"Layer: {name} | Size: {param.size()} | Values : {param[:2]} \n")
            return 0
        except FileNotFoundError:
            print(f"Error: File not found at {path}")
            return -1
        except Exception as e:
            print(f"An error occurred while loading the model: {e}")
            return -1

# if __name__ == '__main__':
#     model = MLP(1,1,[2,3], 'MSE', 'lrelu')
#     model.load_model('mlp.pth')
#     model.train_by_file(r'D:\Sustech\M2PLab\prew\M2PLab\data\CCode\18\124\mlpdata.csv', 2000, 0.01)
#     model.save_model('mlp.pth')
