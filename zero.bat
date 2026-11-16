@echo off

rem jar directory
set AppName=zero-admin.jar

rem JVM options
set JVM_OPTS="-Dname=%AppName%  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps  -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC"

ECHO.
	ECHO.  [1] Start %AppName%
	ECHO.  [2] Stop %AppName%
	ECHO.  [3] Restart %AppName%
	ECHO.  [4] Status %AppName%
	ECHO.  [5] Exit
ECHO.

ECHO.Please select an operation:
set /p ID=
	IF "%id%"=="1" GOTO start
	IF "%id%"=="2" GOTO stop
	IF "%id%"=="3" GOTO restart
	IF "%id%"=="4" GOTO status
	IF "%id%"=="5" EXIT
PAUSE
:start
    for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if defined pid (
		echo %AppName% is running
		PAUSE
	)

start javaw %JVM_OPTS% -jar %AppName%

echo starting...
echo Start %AppName% success...
goto:eof

rem Stop by finding pid with jps.
:stop
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo process %AppName% does not exist) else (
		echo prepare to kill %image_name%
		echo start kill %pid% ...
		rem Kill process by pid.
		taskkill /f /pid %pid%
	)
goto:eof
:restart
	call :stop
    call :start
goto:eof
:status
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo process %AppName% is dead) else (
		echo %image_name% is running
	)
goto:eof
