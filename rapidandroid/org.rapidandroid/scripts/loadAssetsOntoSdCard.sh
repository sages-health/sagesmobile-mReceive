SOURCE="C:\dev\git-repositories\rapidandroid3\rapidandroid\org.rapidandroid\assets\definitions\sdcard"
DEST=/sdcard/rapidandroid

adb devices
adb push "$SOURCE" $DEST