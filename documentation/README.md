## INSTALL

### wikipedia data

    cd ocelot-data/wiki/
    wget https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2
    (wget https://dumps.wikimedia.org/dewiki/latest/dewiki-latest-pages-articles.xml.bz2)
    bzip2 -dk enwiki-latest-pages-articles.xml.bz2
    ./extract-wiki-latest-pages-articles.sh

### dbpedia data
    ./celot-data/dbpedia/download.sh

## Create a new empty index
### solr
    cd ocelot-data
    mkdir solr
    wget https://archive.apache.org/dist/lucene/solr/6.1.0/solr-6.1.0.zip
    unzip solr-6.1.0.zip

#### start solr cloud
    cd ocelot-data/solr/solr-6.1.0
    ./bin/solr start -c -m 3g

#### create core and shards
    ./bin/solr create -c ocelot -shards 5  -p 8983 -d ../../solr-conf/index/conf/

    ./bin/solr create -c surfaceforms -shards 1 -p 8983 -d ../../solr-conf/surfaceforms/conf/

## USE
    cd ocelot/bin/

## compile

    ./compile.sh

## index

    ./index.sh

## run

    ./run.sh
