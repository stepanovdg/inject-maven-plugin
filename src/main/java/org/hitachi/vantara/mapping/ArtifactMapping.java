package org.hitachi.vantara.mapping;

import java.util.Map;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public class ArtifactMapping {
  private String artifactPattern;
  private Map<String,String[]> replaceObjects;
  private Type type;
  private Products[] restart;
  private Components[] clean;

  public String getArtifactPattern() {
    return artifactPattern;
  }

  public void setArtifactPattern( String artifactPattern ) {
    this.artifactPattern = artifactPattern;
  }

  public Map<String, String[]> getReplaceObjects() {
    return replaceObjects;
  }

  public void setReplaceObjects( Map<String, String[]> replaceObjects ) {
    this.replaceObjects = replaceObjects;
  }

  public Type getType() {
    return type;
  }

  public void setType( Type type ) {
    this.type = type;
  }

  public Products[] getRestart() {
    return restart;
  }

  public void setRestart( Products[] restart ) {
    this.restart = restart;
  }

  public Components[] getClean() {
    return clean;
  }

  public void setClean( Components[] clean ) {
    this.clean = clean;
  }
}
