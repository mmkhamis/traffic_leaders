# used libraries
import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras import Sequential
from tensorflow.keras.layers import Dense, LSTM, Dropout

# read the data
data = pd.read_csv('source', date_parser = True)

# specify training and test data
data_training = data['name'].copy()
data_test = data['name'].copy()
data_training = data_training.drop(['Date'], axis = 1)

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

# define first set of data used for forecasting
past_60_intervals = data_training.tail(60)

df = past_60_days.append(data_test, ignore_index = True)
df = df.drop(['Date'], axis = 1)

# scale test data between (0, 1)
inputs = scaler.transform(df)

# split the test data to 60 chunks of data
X_test = []
y_test = []

for i in range(60, inputs.shape[0]):
    X_test.append(inputs[i-60:i])
    y_test.append(inputs[i, 0])

X_test, y_test = np.array(X_test), np.array(y_test)

# predicting
y_pred = regressor.predict(X_test)

# determine scale
scale = 1 / scaler.scale_

# scaling the data to the normal range
y_pred = y_pred*scale # predicted
y_test = y_test*scale # real