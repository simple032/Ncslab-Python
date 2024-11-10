#!/usr/bin/env python
import torch
import torch.nn as nn
import torch.nn.functional as F
import pandas as pd

import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F

class CNN(nn.Module):
    def __init__(self, 
                 n_classes:int, 
                 in_channels:int = 1, 
                 conv_channels=[16, 32], 
                 device='cpu'):
        '''
        @param:
        n_classes: int, the number of classes
        in_channels: int, the number of input channels. Default: 1 (grayscale images)
        
        conv_channels: list, the number of output channels for each convolutional layer
        device: str, the device to run the model on
        '''
        super(CNN, self).__init__()
        self.device = torch.device(device)

        # if conv_channels is None:
        #     conv_channels = [16, 32]

        self.conv_layers = nn.ModuleList()
        self.n_conv_layers = len(conv_channels)

        for out_channels in conv_channels[:n_conv_layers]:
            self.conv_layers.append(nn.Conv2d(in_channels, out_channels, kernel_size=3, stride=1, padding=1))
            self.conv_layers.append(nn.MaxPool2d(kernel_size=2, stride=2, padding=0))
            in_channels = out_channels

        # size of the input to the first fully connected layer
        self.fc_input_size = self._get_fc_input_size()
        self.fc1 = nn.Linear(self.fc_input_size, 128)
        self.fc2 = nn.Linear(128, n_classes)
        self.to(self.device)

    def _get_fc_input_size(self):
        '''
        calculate the size of the input to the first fully connected layer.
        '''
        with torch.no_grad():
            x = torch.randn(1, 1, 224, 224).to(self.device)
            for layer in self.conv_layers:
                x = layer(x)
        return x.numel()

    def forward(self, x):
        '''
        @param:
        
        '''
        x = x.to(self.device)  
        for layer in self.conv_layers:
            x = layer(x)
            if isinstance(layer, nn.Conv2d):
                x = F.relu(x)
        x = x.view(x.size(0), -1)
        x = F.relu(self.fc1(x.to(self.device)))  # 确保在设备上
        x = self.fc2(x)
        return x

    def fit(self, x, y):
        '''
        @param:
        x: torch.Tensor, the input data
        y: torch.Tensor, the labels
        '''
        x = x.to(self.device)
        y = y.to(self.device)
        criterion = nn.CrossEntropyLoss()
        optimizer = torch.optim.Adam(self.parameters(), lr=0.001)
        for epoch in range(10):
            optimizer.zero_grad()
            outputs = self(x)
            loss = criterion(outputs, y)
            loss.backward()
            optimizer.step()
    # def fit(self, dataloader, epochs=10, lr=0.001, momentum=0.9):
    #     self.to(self.device)
    #     criterion = nn.CrossEntropyLoss().to(self.device)
    #     optimizer = optim.SGD(self.parameters(), lr=lr, momentum=momentum)

    #     for epoch in range(epochs):
    #         running_loss = 0.0
    #         for i, data in enumerate(dataloader, 0):
    #             inputs, labels = data
    #             labels -= 1  # 由于我们的标签从1开始，所以我们需要减去1
    #             inputs, labels = inputs.to(self.device), labels.to(self.device)  # 确保数据在同一个设备上
    #             optimizer.zero_grad()
    #             outputs = self(inputs)
    #             loss = criterion(outputs, labels)
    #             loss.backward()
    #             optimizer.step()
    #             running_loss += loss.item()

    #             if i % 100 == 99:
    #                 print(f'[Epoch {epoch + 1}, Mini-batch {i + 1}] loss: {running_loss / 100:.3f}')
    #                 running_loss = 0.0
    #     print('Finished Training')
    def predict(self, x):
        '''
        @param:
        x: torch.Tensor, the input data
        '''
        x = x.to(self.device)
        with torch.no_grad():
            outputs = self(x)
            _, predicted = torch.max(outputs, 1)
        return predicted
    
    def predict_prob(self, x):
        '''
        @param:
        x: torch.Tensor, the input data
        '''
        x = x.to(self.device)
        with torch.no_grad():
            outputs = self(x)
            probabilities = F.softmax(outputs, dim=1)
        return probabilities
    # def predict(self, dataloader):
    #     self.eval()
    #     all_predictions = []
    #     all_labels = []
    #     with torch.no_grad():
    #         for inputs, labels in dataloader:
    #             labels -= 1  # 由于我们的标签从1开始，所以我们需要减去1
    #             inputs, labels = inputs.to(self.device), labels.to(self.device)  # 确保数据在同一个设备上
    #             outputs = self(inputs)
    #             _, predicted = torch.max(outputs, 1)
    #             all_predictions.append(predicted)
    #             all_labels.append(labels)
    #     all_predictions = torch.cat(all_predictions)
    #     all_labels = torch.cat(all_labels)
    #     return all_predictions, all_labels

    def train_by_file(self, filename, epochs, lr):
        # TODO: how to train by img file?
        pass

    def predict_prob(self, dataloader):
        self.eval()
        all_probabilities = []
        all_labels = []
        all_predictions = []
        with torch.no_grad():
            for inputs, labels in dataloader:
                labels -= 1
                inputs, labels = inputs.to(self.device), labels.to(self.device)
                outputs = self(inputs)
                probabilities = F.softmax(outputs, dim=1)
                _, predicted = torch.max(outputs, 1)
                all_probabilities.append(probabilities)
                all_labels.append(labels)
                all_predictions.append(predicted)
        all_probabilities = torch.cat(all_probabilities)
        all_labels = torch.cat(all_labels)
        all_predictions = torch.cat(all_predictions)
        return all_probabilities, all_labels, all_predictions

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
