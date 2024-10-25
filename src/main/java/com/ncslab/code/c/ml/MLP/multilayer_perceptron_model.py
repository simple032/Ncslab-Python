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
                 loss_function = "CE",
                 activation_function = "relu",
                 device='cpu'):
        super(MLP, self).__init__()
        # print("start init")
        self.device = torch.device(device)

        layers = []
        in_features = input_size

        hidden_activate = self._get_activation_function(activation_function)

        for hidden_size in hidden_layers:
            layers.append(nn.Linear(in_features, hidden_size))
            layers.append(hidden_activate)
            in_features = hidden_size

        layers.append(nn.Linear(in_features, output_size))
        self.model = nn.Sequential(*layers)
        # print("model layers: ", layers)
        # print(self.model)
        self.loss_function = self._get_loss_function(loss_function)
        self.to(self.device)
        # print("end init")
        if (isinstance(self.loss_function, nn.CrossEntropyLoss)):
            print("CrossEntropyLoss")
        elif (isinstance(self.loss_function, nn.MSELoss)):
            print("MSELoss")
        print('activation: ', hidden_activate)

    def set_loss_function(self, loss_function):
        self.loss_function = self._get_loss_function(loss_function)

    def forward(self, x):
        x = x.to(self.device)
        return self.model(x)

    def fit(self, X_train, y_train, epochs=100, lr=0.01):
        print('epochs: ', epochs)
        print('lr: ', lr)
        # print("start fit")
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
        # print("end fit")

    # deprecated
    def predict233(self, X):
        # print("start predict")
        # if X is a list:
        if isinstance(X, list):
            X = np.array(X)
        X = torch.tensor(X, dtype=torch.float32)
        X = X.to(self.device)
        self.eval()
        with torch.no_grad():
            outputs = self(X)
        res = [outputs.numpy()[0].tolist()]
        # print(f'res type: {type(res)}, res: {res}.')
        return res

    def predict(self, inputs):
        # print('start predict')
        input_tensor = torch.tensor([inputs], dtype=torch.float32).to(self.device)
        with torch.no_grad():
            output = self(input_tensor)
        return output.numpy()[0].tolist()

    def train_by_file(self, filename, epochs, lr):
        data = pd.read_csv(filename)
        x = torch.tensor(data.iloc[:, :-1].values, dtype=torch.float32)
        y = torch.tensor(data.iloc[:, -1].values, dtype=torch.float32)
        y = y.view(y.shape[0], 1)
        self.fit(x, y, epochs, lr)

    def _get_loss_function(self, loss_function):
        if (loss_function == 'CE'):
            return nn.CrossEntropyLoss()
        elif (loss_function == 'MSE'):
            return nn.MSELoss()
        else:
            return nn.CrossEntropyLoss()

    def _get_activation_function(self, activation_function):
        if (activation_function == 'relu'):
            return nn.ReLU()
        elif (activation_function == 'sigmoid'):
            return nn.Sigmoid()
        elif (activation_function == 'tanh'):
            return nn.Tanh()
        elif (activation_function == 'lrelu'):
            return nn.LeakyReLU()
        else:
            return nn.ReLU()

    def save_model(self, path):
        try:
            model_info = {
              'state_dict': self.state_dict(),  # 保存模型的权重
              'input_size': self.model[0].in_features,  # 输入特征数
              'output_size': self.model[-1].out_features,  # 输出特征数
              'hidden_layers': [layer.out_features for layer in self.model if isinstance(layer, nn.Linear)][:-1],  # 隐藏层大小
              'loss_function': self.loss_function.__class__.__name__,  # 保存损失函数的名称
              'activation_function': self.model[1].__class__.__name__  # 保存激活函数的名称
            }
            # 保存模型到指定的路径
            torch.save(model_info, path)
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
            model_info = torch.load(path, map_location=self.device)

            # Extract input and output feature sizes from the saved model
            input_size = model_info['input_size']
            output_size = model_info['output_size']
            hidden_layers = model_info['hidden_layers']

            # Extract loss function and activation function names
            loss_function_name = model_info.get('loss_function', 'MSE')
            activation_function_name = model_info.get('activation_function', 'ReLU')

            # Rebuild the model structure with the saved parameters if necessary
            if input_size != self.model[0].in_features or output_size != self.model[-1].out_features:
                self.__init__(input_size, output_size, hidden_layers,
                              loss_function=loss_function_name,
                              activation_function=activation_function_name,
                              device=self.device)

            # Load the saved state dictionary (weights)
            self.load_state_dict(model_info['state_dict'])

            # Set the loss function based on the saved model info
            self.loss_function = self._get_loss_function(loss_function_name)

            print(f"Model loaded successfully from {path}")
            return 0
        except FileNotFoundError:
            print(f"Error: File not found at {path}")
            return -1
        except Exception as e:
            print(f"An error occurred while loading the model: {e}")
            return -1

