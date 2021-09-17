@echo off

@call .\setenv.bat

@call generate_test_code.bat

pushd %HAZELCAST_ADDON_DIR%\build
javac *.java
@popd 
@echo HQL Antrl4 code generated and compiled in the following directory:
@echo    %HAZELCAST_ADDON_DIR%\build
@echo To run grun_hql, change directory as follows:
@echo    cd %HAZELCAST_ADDON_DIR%\build
@echo    grun_hql ..\test\hql\entries1.txt
