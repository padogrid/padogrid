@echo off

@call .\setenv.bat

@call antlr4.bat -visitor -o %PACKAGE_DIR% -package %PACKAGE_NAME% %HQL_G4_PATH%
