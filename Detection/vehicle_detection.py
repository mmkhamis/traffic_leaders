# used libraries
import cv2 as cv

# identify the recording
vid = cv.VideoCapture('Detection/Recordings/video1.avi')

# identify haar cascade classifiers
car_cascade = cv.CascadeClassifier('Detection/Vehicle_and_pedestrain_haar_cascades/cars.xml')
bus_front_cascade = cv.CascadeClassifier('Detection/Vehicle_and_pedestrain_haar_cascades/Bus_front.xml')
two_wheels_cascade = cv.CascadeClassifier('Detection/Vehicle_and_pedestrain_haar_cascades/two_wheeler.xml')
pedestrain_cascade = cv.CascadeClassifier('Detection/Vehicle_and_pedestrain_haar_cascades/pedestrian.xml')

count = 0 # counter

while True:
    ret, frame = vid.read() # read frames of the recordings

    # close when video ends
    if (type(frame) == type(None)):
        break

    # turn frame to blur gray
    blur = cv.blur(frame, (3, 3))
    gray = cv.cvtColor(blur, cv.COLOR_BGR2GRAY) 

    # detect vehicles (cars, buses, two wheels) and pedestrains
    cars = car_cascade.detectMultiScale(gray,1.01, 1)
    buses = bus_front_cascade.detectMultiScale(gray,1.01, 1)
    twoWheels = two_wheels_cascade.detectMultiScale(gray,1.01, 1)
    pedestrains = pedestrain_cascade.detectMultiScale(gray,1.01, 1)

    # draw rectangles around them
    for (x, y, w, h) in cars:
        cv.rectangle(frame, (x,y), (x+w,y+h), (0, 0, 255), 2) # red for cars

    for (x, y, w, h) in buses:
        cv.rectangle(frame, (x,y), (x+w,y+h), (255, 0, 0), 2) # blue for buses

    for (x, y, w, h) in twoWheels:
        cv.rectangle(frame, (x,y), (x+w,y+h), (0, 255, 255), 2) # yellow for two wheels

    for (x, y, w, h) in pedestrains:
        cv.rectangle(frame, (x,y), (x+w,y+h), (0, 255, 0), 2) # green for pedestrains

    # display final frames
    cv.imshow('Video', frame)

    # condition of pressing 'ESC' to exit the loop and close the program
    if cv.waitKey(1) == 27:
        break

# close everything
cv.destroyAllWindows() 