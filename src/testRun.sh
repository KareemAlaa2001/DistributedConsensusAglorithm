#!/bin/sh

# java UDPLoggerServer 12344 &
# echo "Waiting for loggerserver to start..."
# sleep 5
java Coordinator 12345 12344 4 500 A B &
echo "Waiting for coordinator to start..."
sleep 5
java Participant 12345 12344 12346 500 &
sleep 1
java Participant 12345 12344 12347 500 &
sleep 1
java Participant 12345 12344 12348 500 &
sleep 1
java Participant 12345 12344 12349 500 &
