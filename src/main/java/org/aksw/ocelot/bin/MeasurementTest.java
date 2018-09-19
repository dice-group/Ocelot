package org.aksw.ocelot.bin;

import java.util.Map;

import org.aksw.ocelot.core.measure.MeasurementExperiment;

public class MeasurementTest {

  public static void main(final String[] args) {
    final MeasurementExperiment ex = new MeasurementExperiment();
    final int n = 5;
    final Map<String, Map<String, Double>> scores = ex.scores(n);
    ex.print(scores);
  }
}
