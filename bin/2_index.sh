export MAVEN_OPTS="-Xmx100G"
cd ..
nohup mvn exec:java -Dexec.mainClass="org.aksw.nlp.re.ocelot.bin.Indexer" > bin/index.log &
cd bin
