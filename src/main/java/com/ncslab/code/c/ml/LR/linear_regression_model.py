#!/usr/bin/env python
import torch
import torch.nn as nn
import numpy as np
import pandas as pd

class LinearRegression(nn.Module):
    def __init__(self, input_features, output_features, device='cpu'):
        super(LinearRegression, self).__init__()
        self.linear = nn.Linear(input_features, output_features)
        self.device = torch.device(device)

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
        criterion = nn.MSELoss()
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