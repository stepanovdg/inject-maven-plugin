package org.hitachi.vantara.mapping;

import java.util.Map;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public class ProjectMapping {

  private Map<String, ArtifactMapping> artifactsMappings;


  public Map<String, ArtifactMapping> getArtifactsMappings() {
    return artifactsMappings;
  }

  public void setArtifactsMappings(
    Map<String, ArtifactMapping> artifactsMappings ) {
    this.artifactsMappings = artifactsMappings;
  }


}
