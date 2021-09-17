@echo off

@call .\setenv.bat

if not exist "%HAZELCAST_ADDON_DIR%\build" (
   @mkdir %HAZELCAST_ADDON_DIR%\build
)
@pushd %HAZELCAST_ADDON_DIR%\build
@call antlr4 -visitor -o . %HQL_G4_PATH%
@popd
