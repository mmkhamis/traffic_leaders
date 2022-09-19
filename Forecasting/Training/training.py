# used libraries
import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras import Sequential
from tensorflow.keras.layers import Dense, LSTM, Dropout

# read the data
data = pd.read_csv('Dataset/data.csv', date_parser = True)

# determine the starting point
start_index = list(data['5 Minutes']).index('18/03/2020 23:55')

# specify training and test data
data_training = data[0 : start_index + 1].copy()
data_test = data[start_index + 1 : len(data)].copy()

data_training = data_training.drop(['5 Minutes'], axis = 1)
data_test = data_test.drop(['5 Minutes'], axis = 1)

# scale training data between (0, 1)
scaler = MinMaxScaler()
data_training = scaler.fit_transform(data_training)

# split the trainig data to 60 chunks of data
X_train = []
y_train = []

for i in range(60, data_training.shape[0]):
    X_train.append(data_training[i-60:i])
    y_train.append(data_training[i, 0])
    
X_train, y_train = np.array(X_train), np.array(y_train)

# define LSTM model 
regressor = Sequential()

regressor.add(LSTM(units = 60, activation = 'relu', return_sequences = True, input_shape = (X_train.shape[1], 1)))
regressor.add(Dropout(0.2))

regressor.add(LSTM(units = 80, activation = 'relu', return_sequences = True))
regressor.add(Dropout(0.2))

regressor.add(LSTM(units = 120, activation = 'relu'))
regressor.add(Dropout(0.2))

regressor.add(Dense(units = 1))

# compile the model and fit it into the training data
regressor.compile(optimizer='adam', loss = 'mean_squared_error')
regressor.fit(X_train, y_train, epochs=50, batch_size=32)

# save LSTM model
regressor.save('Forecasting/Training/Model/lstm_model.h5')