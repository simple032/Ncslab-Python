#!/usr/bin/env python
import pandas as pd
import torch
import torch.nn as nn
import numpy as np

class LogisticRegression(nn.Module):
    def __init__(self, input_features, output_features, loss_function = 'CE', device='cpu'):
        super(LogisticRegression, self).__init__()
        self.linear = nn.Linear(input_features, output_features)
        self.device = torch.device(device)
        self.loss_function = self._get_loss_function(loss_function)
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        return self.linear(x)

    def fit(self, X_train, y_train, epochs=100, lr=0.01):
        X_train = X_train.to(self.device)
        y_train = y_train.to(self.device)

        # Loss and optimizer
        criterion = nn.CrossEntropyLoss()
        optimizer = torch.optim.SGD(self.parameters(), lr=lr)

        for epoch in range(epochs):
            self.train()

            # Forward pass
            outputs = self(X_train)
            loss = criterion(outputs, y_train)

            # Backward pass and optimization
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

    def train_by_file(self, filename, epochs, lr):
        data = pd.read_csv(filename)
        x = torch.tensor(data.iloc[:, :-1].values, dtype=torch.float32)
        y = torch.tensor(data.iloc[:, -1].values, dtype=torch.float32)
        y = y.view(y.shape[0], 1)
        self.fit(x, y, epochs, lr)

    def predict(self, X):
        X = np.array(X)
        X = torch.tensor(X, dtype=torch.float32)
        X = X.to(self.device)
        if X.dim() == 1:
            X = X.unsqueeze(0)
        self.eval()
        with torch.no_grad():
            outputs = self(X)
            # print(f"Shape of outputs: {outputs.shape}")  # Debug print statement
            if outputs.dim() == 1:
                probabilities = nn.Softmax(dim=0)(outputs)
            else:
                probabilities = nn.Softmax(dim=1)(outputs)
            _, predicted = torch.max(probabilities, 1)
        return [float(predicted.to('cpu').numpy()[0])]
    
    def _get_loss_function(self, loss_function):
        if (loss_function == 'CE'):
            return nn.CrossEntropyLoss()
        elif (loss_function == 'MSE'):
            return nn.MSELoss()
        else:
            return nn.MSELoss()

