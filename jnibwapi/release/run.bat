REM change back to the current directory (if run as administrator)
cd %~dp0
REM start the java bot (you may need to specify 32-bit java here)
java -jar jnibwapi.jar
pause
