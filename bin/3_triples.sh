export MAVEN_OPTS="-Xmx100G"
cd ..
nohup mvn exec:java -Dexec.mainClass="org.aksw.ocelot.bin.Triples" > bin/triples.log &
cd bin
