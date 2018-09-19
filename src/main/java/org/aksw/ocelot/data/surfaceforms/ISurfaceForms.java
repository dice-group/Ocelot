package org.aksw.ocelot.data.surfaceforms;

import java.util.Set;

public interface ISurfaceForms {

  /**
   * Returns all surfaceforms for the given resource.
   *
   * @param uri
   * @return surfaceforms
   */
  public Set<String> getSurfaceform(String uri);
}
