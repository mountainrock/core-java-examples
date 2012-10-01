set CATALINA_HOME=D:\Sandeep\tools\Servers\tomcat\apache-tomcat-6.0.24
rmdir /s /q %CATALINA_HOME%\webapps\coacs

cmd /c mvn clean install
cmd /c %CATALINA_HOME%\bin\shutdown.bat
cmd /c copy target\*.war %CATALINA_HOME%\webapps
cmd /c %CATALINA_HOME%\bin\startup.bat