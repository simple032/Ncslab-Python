#!/usr/bin/env python
import torch
import torch.nn as nn
import torch.nn.functional as F
import pandas as pd

class SimpleCNN(nn.Module):
    def __init__(self, num_classes, device='cpu'):
        super(SimpleCNN, self).__init__()
        self.conv1 = nn.Conv2d(in_channels=1, out_channels=32, kernel_size=3, stride=1, padding=1)
        self.conv2 = nn.Conv2d(in_channels=32, out_channels=64, kernel_size=3, stride=1, padding=1)
        self.fc1 = nn.Linear(64 * 7 * 7, 128)
        self.fc2 = nn.Linear(128, num_classes)
        self.device = torch.device(device)
        self.to(self.device)

    def forward(self, x):
        x = F.relu(self.conv1(x))
        x = F.max_pool2d(x, 2)
        x = F.relu(self.conv2(x))
        x = F.max_pool2d(x, 2)
        x = x.view(x.size(0), -1)
        x = F.relu(self.fc1(x))
        x = self.fc2(x)
        return x
    
    def train_by_file(self, filename, epochs, lr):
        data = pd.read_csv(filename)
        x = torch.tensor(data.iloc[:, :-1].values, dtype=torch.float32).view(-1, 1, 28, 28)
        y = torch.tensor(data.iloc[:, -1].values, dtype=torch.long)
        self.fit(x, y, epochs, lr)

    def fit(self, x, y, epochs, lr):
        x = x.to(self.device)
        y = y.to(self.device)
        criterion = nn.CrossEntropyLoss()
        optimizer = torch.optim.Adam(self.parameters(), lr=lr)
        for epoch in range(epochs):
            optimizer.zero_grad()
            outputs = self(x)
            loss = criterion(outputs, y)
            loss.backward()
            optimizer.step()
        
    def predict(self, inputs):
        input_tensor = torch.tensor([inputs], dtype=torch.float32).view(-1, 1, 28, 28).to(self.device)
        with torch.no_grad():
            output = self(input_tensor)
        return output.argmax(dim=1).cpu().numpy().tolist()

# Example usage:
# model = SimpleCNN(num_classes=10)
# model.train_by_file('train.csv', epochs=10, lr=0.001)
# prediction = model.predict(input_image)  # input_image should be a flattened image of size 28*28
