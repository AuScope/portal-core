@ECHO OFF

IF "%1" == "" GOTO BUILD_RUN
IF "%1" == "-b" GOTO BUILD
IF "%1" == "-r" GOTO RUN
GOTO END

:BUILD
call mvn package
GOTO END

:RUN
mvn jetty:run
GOTO END

:BUILD_RUN
call mvn jetty:run
:END
