# streaming_log
keep read log file while file is update with kotlin

# build
```$bash
kotlinc -cp src/:out/:. src/gui/*.kt -d StreamLog.jar
kotlin -cp StreamLog.jar:$JAVA_HOME/lib/ext/jfxrt.jar gui.StreamLogGuiMainKt
```
