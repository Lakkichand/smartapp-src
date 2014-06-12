#!/bin/sh
echo Packaging uid=$1, please wait.....
echo $1 | tee res/raw/uid.txt > res/raw/gostore_uid.txt
/usr/lib/eclipse/plugins/org.apache.ant_1.8.2.v20120109-1030/bin/ant -buildfile build-sign.xml
cp ./bin/go_launcher_ex-release.apk ./apk/go_launcher_ex_v$2_$1.apk
rm ./bin/*.apk
echo *********package go_launcher_ex_v$2_$1.apk success****************
