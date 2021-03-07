:loop
git stash
git pull
java -jar .\d4dj_rankTracker.jar -Xmx1024m
goto loop