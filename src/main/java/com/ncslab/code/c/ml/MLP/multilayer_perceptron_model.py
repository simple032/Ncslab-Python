#!/usr/bin/env python
import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
import pandas as pd
import numpy as np

class MLP(nn.Module):
    def __init__(self, input_size, output_size, hidden_layers, loss_function = "CE", device='cpu'):
        super(MLP, self).__init__()
        # print("start init")
        self.device = torch.device(device)

        layers = []
        in_features = input_size
        for hidden_size in hidden_layers:
            layers.append(nn.Linear(in_features, hidden_size))
            layers.append(nn.ReLU())
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
            # probabilities = F.softmax(outputs, dim=1)
            # _, predicted = torch.max(probabilities, 1)
        # return predicted.to(self.device)
        # print("end predict")
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
        # loss_functions = {
        #     'CE': nn.CrossEntropyLoss(),
        #     'MSE': nn.MSELoss()
        #     # 'bce': nn.BCELoss()
        # }
        # return loss_functions.get(loss_function.lower(), nn.CrossEntropyLoss())
        if (loss_function == 'CE'):
            return nn.CrossEntropyLoss()
        elif (loss_function == 'MSE'):
            return nn.MSELoss()
        else:
            return nn.CrossEntropyLoss()
        
    def _get_activation_function(self, activation_function):
        activation_functions = {
            'relu': nn.ReLU(),
            'sigmoid': nn.Sigmoid(),
            'tanh': nn.Tanh(),
            'lrelu': nn.LeakyReLU(),
            'none': nn.Identity()  # No activation function
        }
        return activation_functions.get(activation_function.lower(), nn.ReLU())

    def set_activation_function(self, activation_function):
        self.activation_function = self._get_activation_function(activation_function)
        print('current activation: ', activation_function)
        self._rebuild_model()

    def _rebuild_model(self):
        layers = []
        in_features = self.model[0].in_features
        for layer in self.model:
            if isinstance(layer, nn.Linear):
                layers.append(layer)
                if layer != self.model[-1]:
                    layers.append(self.activation_function)
        self.model = nn.Sequential(*layers)
        self.to(self.device)

