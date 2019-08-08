export MAVEN_OPTS="-Xmx10G"
cd ..
nohup mvn exec:java -Dexec.mainClass="org.aksw.ocelot.bin.IndexerTest" > bin/index.log &
cd bin
