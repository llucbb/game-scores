Thoughts: The code is well documented with my decisions.

Considerations: The server will scale as is using a fixed thread pool executor, which creates a thread pool that reuses
a fixed number of threads (by default number of system cores - 1, but can be defined). Although the server cannot scale
horizontally due to the usage of data in memory.

Requirements: JDK 1.8: Using newer version could produce the java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter
executing the jar. The JAXB APIs are considered to be Java EE APIs and therefore are no longer contained on the default
classpath in Java SE 9.

Libs used for unit testing:
junit-4.12.jar
hamcrest-core-2.1.jar
hamcrest-2.1.jar

Execution:

java -jar game-scores.jar

Ensure the user executing the jar has write access to the folder where the jar is located to avoid issues. A log
will be displayed in the console and a game-scores-log will be created in the same location. This configuration could be
changed at com.king.gamescores.log.ScoresLogger.java class.
