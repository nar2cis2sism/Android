set keystore=androidEngine.keystore
set file=../../build\jar\GameEngine.jar

jarsigner -storetype jks -keystore %keystore% -verbose %file% androidEngine

pause