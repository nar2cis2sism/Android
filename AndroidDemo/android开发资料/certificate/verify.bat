set file=../../build\jar\GameEngine.jar

jarsigner -verify -verbose -certs %file%

pause