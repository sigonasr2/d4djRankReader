:loop
git stash
git pull
java -jar .\d4dj_rankTracker.jar -XX:MaxPermSize=256m -Xms128m -Xmx1024m
goto loop