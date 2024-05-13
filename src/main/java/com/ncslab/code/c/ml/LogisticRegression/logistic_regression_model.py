#!/usr/bin/env python
import torch
import torch.nn as nn
import numpy as np
import pandas as pd

class LogisticRegression(nn.Module):
    def __init__(self, input_features, output_features):
        super(LogisticRegression, self).__init__()
        self.linear = nn.Linear(input_features, output_features)
        self.sigmoid = nn.Sigmoid()

    def forward(self, x):
        return self.sigmoid(self.linear(x))

ml_model = LogisticRegression(3, 1)

def init_model():
    ml_model = LogisticRegression(3, 1)
    return ml_model

def train_model(filename, epochs, lr):
    data = pd.read_csv(filename)
    x = torch.tensor(data.iloc[:, :-1].values, dtype=torch.float32)
    y = torch.tensor(data.iloc[:, -1].values, dtype=torch.float32)
    y = y.view(y.shape[0], 1)
    criterion = nn.BCELoss()
    optimizer = torch.optim.SGD(ml_model.parameters(), lr=lr)
    for epoch in range(epochs):
        optimizer.zero_grad()
        outputs = ml_model(x)
        loss = criterion(outputs, y)
        loss.backward()
        optimizer.step()

def predict(inputs):
    input_tensor = torch.tensor([inputs], dtype=torch.float32)
    with torch.no_grad():
        output = ml_model(input_tensor)
    return output.numpy()[0].tolist()
