all:
	javac ACOMusic.java
	javac GAMusic.java GAMain.java Organism.java

runACO:
	java ACOMusic

runGA:
	java GAMain
