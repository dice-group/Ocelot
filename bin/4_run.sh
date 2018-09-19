#export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export MAVEN_OPTS="-Xmx100G -Djava.library.path=lib/rJava"
cd ..
#nohup mvn exec:java -Dexec.mainClass="org.aksw.nlp.ocelot.Main"  -Djava.library.path="lib/rJava"> bin/run.log &
nohup mvn exec:java -Dexec.mainClass="org.aksw.ocelot.bin.Run" > bin/run.log &
cd bin
