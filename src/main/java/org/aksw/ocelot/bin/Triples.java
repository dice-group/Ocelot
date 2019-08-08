package org.aksw.ocelot.bin;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.kb.DBpediaKB;
import org.aksw.ocelot.data.properties.PropertiesFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Triples {
  protected final static Logger LOG = LogManager.getLogger(Triples.class);

  public static void main(final String[] args) {
    LOG.info("Start ...");

    final DBpediaKB kb = new DBpediaKB();

    PropertiesFactory.getInstance(PropertiesFactory.file)//
        .getPredicates()//
        .parallelStream()//
        .forEach(p -> kb.getTriples(p, Const.maxTriplesperURI)//
        );

    LOG.info("End ...");
  }
}
