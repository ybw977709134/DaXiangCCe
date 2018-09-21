#!/bin/bash
echo "======================"
if [ ! -n "$1" ];then
	packageName="com.daxiangce123"
	pid=`adb shell ps | grep $packageName | awk '{print $2}'`
	echo "grep com.daxiangce123 PID is $pid"
else
	pid=$1
	echo "only grep $pid"
fi
echo "======================"
adb logcat | grep --color=always $pid
