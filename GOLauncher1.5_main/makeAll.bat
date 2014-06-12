@IF NOT EXIST gen mkdir gen
@IF EXIST apk RD /S /Q apk
@mkdir apk

::输入版本号
@echo 请输入版本号：
@set /p version=
@echo ********************* GOLauncherEX版本号为 %version% *********************

@echo off
@set /a StartS=%time:~6,2%
@set /a StartM=%time:~3,2%

::先编译代码和库, 打包和签名由build.bat完成
@call D:\Android\eclipse-SDK-3.6-win32\eclipse\plugins\org.apache.ant_1.7.1.v20100518-1145\bin\ant.bat -buildfile .\build-compile.xml

call build.bat 301 %version%
call build.bat 311 %version%
call build.bat 314 %version%
call build.bat 325 %version%
call build.bat 351 %version%
call build.bat 353 %version%
call build.bat 357 %version%
call build.bat 322 %version%
call build.bat 302 %version%
call build.bat 303 %version%
call build.bat 328 %version%
call build.bat 331 %version%
call build.bat 500 %version%
call build.bat 529 %version%

::此处重复循环打包，仅需修改(200, 1, 210) ，200 为其实id，210为结束id，1为增量

::201-210 10个
for /L %%i in (201, 1, 210) do call build.bat %%i %version%

::311-315 5个
for /L %%i in (311, 1, 315) do call build.bat %%i %version%

::317-318 2个
for /L %%i in (317, 1, 318) do call build.bat %%i %version%

::350-370  21个
for /L %%i in (350, 1, 370) do call build.bat %%i %version%

::301-310 十个
for /L %%i in (301, 1, 310) do call build.bat %%i %version%

::400-419 20个
for /L %%i in (400, 1, 419) do call build.bat %%i %version%

:: 321
call build.bat 321 %version%

::323-350 28个
for /L %%i in (323, 1, 350) do call build.bat %%i %version%

::371-399  20个
for /L %%i in (371, 1, 399) do call build.bat %%i %version%

::500-560  61个
for /L %%i in (500, 1, 560) do call build.bat %%i %version%

::450-470  21个
for /L %%i in (450, 1, 470) do call build.bat %%i %version%

::420-450 31个
for /L %%i in (420, 1, 450) do call build.bat %%i %version%

>.\res\raw\gostore_uid.txt set/p=200<nul
>.\res\raw\uid.txt set/p=200<nul

::计算打包时间
@set /a EndS=%time:~6,2%
@set /a EndM=%time:~3,2%

@set /a diffS_=%EndS%-%StartS%
@set /a diffM_=%EndM%-%StartM%

@if %diffS_% LSS 0 (
@set /a diffM_ = %diffM_% - 1
@set /a diffS_ = 60 + %diffS_%
)
@echo 打包共耗时%diffM_%分%diffS_%秒

pause