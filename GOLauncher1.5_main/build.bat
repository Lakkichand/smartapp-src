@echo Packaging uid = %1.txt, please wait...
>.\res\raw\gostore_uid.txt set/p=%1<nul
>.\res\raw\uid.txt set/p=%1<nul
@call D:\Android\apache-ant-1.8.2\bin\ant.bat -buildfile .\build-sign.xml
copy .\bin\go_launcher_ex-release.apk .\apk\go_launcher_ex_v%2_%1.apk
del .\bin\*.apk
@echo !!! Packaging %1.txt success !!!
