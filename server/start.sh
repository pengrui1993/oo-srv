#!/bin/bash

#java -jar -server -Xmx1g -Xms1g -Xss512k -Dspring.profiles.active=prod  1>/dev/null 2>&1 &
nohup java -server -jar server-0.0.1-plain.jar 1>/dev/null 2>&1 &

# cat application.pid|xargs kill -9