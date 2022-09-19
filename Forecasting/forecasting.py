# used libraries
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras import Sequential
from tensorflow.keras.layers import Dense, LSTM, Dropout
from tensorflow.keras.models import load_model
import pyrebase
import json

# firebase real-time database configuration
firebaseConfig = {
    "apiKey": "AIzaSyC_0H97G-CgyJhkDsisHhLtu6fLh5ipgLw",
    "authDomain": "stem-community.firebaseapp.com",
    "databaseURL": "https://stem-community.firebaseio.com",
    "projectId": "stem-community",
    "storageBucket": "stem-community.appspot.com",
    "messagingSenderId": "1043923929656",
    "appId": "1:1043923929656:web:b77a8d22ddc73df93bf4cd"
  }
firebase = pyrebase.initialize_app(firebaseConfig)
db = firebase.database()

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
scaler = MinMaxScaler(feature_range= (0,1))
data_training = scaler.fit_transform(data_training)
data_training = pd.DataFrame(data_training)

# define first set of data used for forecasting
past_60_days = data_training.tail(60)
df = past_60_days.append(pd.DataFrame(scaler.fit_transform(data_test)), ignore_index = True)

# scale test data between (0, 1)
inputs = scaler.fit_transform(df)

# split the test data to 60 chunks of data
X_test = []
y_test = []

for i in range(60, inputs.shape[0]):
    X_test.append(inputs[i-60:i])
    y_test.append(inputs[i, 0])

X_test, y_test = np.array(X_test), np.array(y_test)

# load LSTM model
regressor = load_model('Forecasting/Training/Model/lstm_model.h5')

# predicting
y_pred = regressor.predict(X_test)

# scaling the data to the normal range
y_pred = pd.DataFrame(y_pred)
y_pred = round( y_pred / 0.005614973 )
y_test = pd.DataFrame(y_test)
y_test = round( y_test / 0.005614973 )

# push data to firebase
predicted = y_pred.to_numpy()
real = y_test.to_numpy()

db.child("Predicted").set({0 : int(real[0])})
for i in range(1, predicted.shape[0]):
  x = int(predicted[i])
  db.child("Predicted").update({i : x})

db.child("Real").set({0 : int(real[0])})
for i in range(1, real.shape[0]):
  y = int(real[i])
  db.child("Real").update({i : y})

# push date and time
date_and_time = data[start_index + 1 : len(data)]['5 Minutes'].copy()
counter = 1
db.child("Date and Time").child("date").set({0 : date_and_time[start_index + 1].split()[0]})
db.child("Date and Time").child("time").set({0 : date_and_time[start_index + 1].split()[1]})

for i in range(start_index + 2, len(data)):
  db.child("Date and Time").child("date").update({counter : date_and_time[i].split()[0]})
  counter += 1

counter = 1

for i in range(start_index + 2, len(data)):
  db.child("Date and Time").child("time").update({counter : date_and_time[i].split()[1]})
  counter += 1

# Visualizing data
plt.figure(figsize=(14,5))
plt.plot(y_test, color = 'red', label = 'Real traffic flow')
plt.plot(y_pred, color = 'blue', label = 'Predicted traffic flow')
plt.title('Traffic Forecasting')
plt.xlabel('Time')
plt.ylabel('Traffic flow')
plt.legend()
plt.show()

# save the predicted data and real data to csv
y_pred.to_csv('Forecasting/predicted.csv')
y_test.to_csv('Forecasting/real.csv')