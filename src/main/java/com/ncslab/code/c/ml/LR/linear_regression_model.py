#!/usr/bin/env python
import torch
import torch.nn as nn
import numpy as np
import pandas as pd

class LinearRegression(nn.Module):
    def __init__(self, input_features, output_features):
        super(LinearRegression, self).__init__()
        self.linear = nn.Linear(input_features, output_features)

    def forward(self, x):
        return self.linear(x)

model = LinearRegression(3, 1)

def init_model():
    global model
    model = LinearRegression(3, 1)
    return model

def train_model(filename, epochs, lr):
    data = pd.read_csv(filename)
    x = torch.tensor(data.iloc[:, :-1].values, dtype=torch.float32)
    y = torch.tensor(data.iloc[:, -1].values, dtype=torch.float32)
    y = y.view(y.shape[0], 1)
    criterion = nn.MSELoss()
    optimizer = torch.optim.SGD(model.parameters(), lr=lr)
    for epoch in range(epochs):
        optimizer.zero_grad()
        outputs = model(x)
        loss = criterion(outputs, y)
        loss.backward()
        optimizer.step()

def predict(inputs):
    input_tensor = torch.tensor([inputs], dtype=torch.float32)
    with torch.no_grad():
        output = model(input_tensor)
    return output.numpy()[0].tolist()
