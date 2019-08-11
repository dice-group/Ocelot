#!/bin/bash

#http://downloads.dbpedia.org/2016-04/core-i18n/en/
wget http://downloads.dbpedia.org/2016-04/core-i18n/de/redirects_de.tql.bz2
wget http://downloads.dbpedia.org/2016-04/core-i18n/de/mappingbased_properties_de.tql.bz2
wget http://downloads.dbpedia.org/2016-04/core-i18n/de/instance_types_de.tql.bz2
wget http://downloads.dbpedia.org/2016-04/core-i18n/de/labels_de.tql.bz2
wget http://downloads.dbpedia.org/2016-04/core-i18n/de/disambiguations_de.tql.bz2

bzip2 -dk ./*.bz2

#wget https://raw.githubusercontent.com/okbqa/agdistis-disambiguation/master/resources/dbpedia_3Eng_property.ttl

