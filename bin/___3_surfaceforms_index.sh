export MAVEN_OPTS="-Xmx100G"
cd ..
nohup mvn exec:java -Dexec.mainClass="org.aksw.nlp.re.ocelot.surfaceforms.index.MainSurfaceformsSolrIndex" > bin/sufindex.log &
cd bin
