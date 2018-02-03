#!/bin/bash
while true
do
  java -jar -Xmx512m target/xmppbot.exe &
  wait $!
done
