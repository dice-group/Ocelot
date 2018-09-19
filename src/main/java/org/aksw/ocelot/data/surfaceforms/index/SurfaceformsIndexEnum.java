package org.aksw.ocelot.data.surfaceforms.index;

public enum SurfaceformsIndexEnum {

  URI("uri"),

  SFS("surfaceforms"),

  SFSSize("sfssize");

  private String name;

  private SurfaceformsIndexEnum(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
