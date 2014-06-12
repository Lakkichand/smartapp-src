#!/bin/sh
date_start=$(date +%s)
if [ ! -d "gen" ]; then  
mkdir "gen"  
fi  

if [ -d "apk" ];then
rm -r "apk"
fi
mkdir "apk"
echo "input versioncode"
read version
echo "******************** build GOLauncherEX v$version *******************"
/usr/lib/eclipse/plugins/org.apache.ant_1.8.2.v20120109-1030/bin/ant -buildfile ./build-compile.xml

sh build.sh 301 $version
#sh build.sh 353 $version

#201 ~ 210
for (( i=201; i<210; i++));do
sh build.sh $i $version
done

#for (( i=301; i<310; i++));do
#sh build.sh $i $version
#done

echo "************************all APK have been packaged**********************"
echo "200" | tee res/raw/uid.txt > res/raw/gostore_uid.txt
date_end=$(date +%s)
echo "Total time $(($((date_end-date_start)) / 60))m"
exit
