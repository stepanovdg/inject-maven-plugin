package org.hitachi.vantara.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.hitachi.vantara.mapping.ArtifactMapping;
import org.hitachi.vantara.mapping.ProjectMapping;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public class YAMLMappingSelectorBuilder extends ZipArtifactSelectorBuilder {

  private String artifactId;
  private ProjectMapping mapping;
  private ArtifactMapping artifactMapping;
  private MavenProject project;

  public YAMLMappingSelectorBuilder( FileSystemManager fsManager, Artifact producedArtifact, FileObject targetDir,
                                     MavenProject project ) {
    super( fsManager, producedArtifact, targetDir );
    this.artifactId = project.getArtifactId();
    this.project = project;
  }

  public boolean init( ProjectMapping mappingIn ) throws IOException {
    this.mapping = mappingIn;
    if ( mapping.getArtifactsMappings() == null ) {
      return false;
    }
    artifactMapping = mapping.getArtifactsMappings().get( artifactId );
    if ( artifactMapping == null ) {
      return false;
    }
    return true;
  }

  public Map<FileObject, List<FileObject>> findObjectsToReplace( Map<FileObject, List<FileObject>> ret,
                                                                 FileObject outputDir ) throws FileSystemException {
    Map<String, String[]> replaceObjects = artifactMapping.getReplaceObjects();
    if ( replaceObjects != null ) {
      switch ( artifactMapping.getType() ) {
        case ZIP:
        case TARGZ: {
          String zipArt = artifactMapping.getArtifactPattern().replace( "${VERSION}", project.getVersion() );
          FileObject zip = fsManager.resolveFile( targetDir, zipArt );
          String zipUrlInsideFormat = artifactMapping.getType().schema + zip.getURL().toString() + "!/%s";
          for ( String artString : replaceObjects.keySet() ) {
            String artUrlInZip = String.format( zipUrlInsideFormat, artString );
            FileObject art = fsManager.resolveFile( artUrlInZip );
            return processArtifacts( outputDir, ret, replaceObjects, artString, art );
          }
          break;
        }
        default: {
          for ( String artString : replaceObjects.keySet() ) {
            FileObject art = fsManager.resolveFile( targetDir, artString );
            return processArtifacts( outputDir, ret, replaceObjects, artString, art );
          }
          break;
        }
      }
    }

    return Collections.emptyMap();
  }

  private Map<FileObject, List<FileObject>> processArtifacts( FileObject outputDir,
                                                              Map<FileObject, List<FileObject>> ret,
                                                              Map<String, String[]> replaceObjects, String artString,
                                                              FileObject art )
    throws FileSystemException {
    List<String> targetPlaces = Arrays.asList( replaceObjects.get( artString ) );
    List<FileObject> replaceFileObjects = new ArrayList<FileObject>();
    for ( String targetPlace : targetPlaces ) {
      FileObject replace = outputDir.resolveFile( targetPlace.replace( "${VERSION}", project.getVersion() ) );
      replaceFileObjects.add( replace );
    }
    putUpdate( ret, art, replaceFileObjects );
    return getFileObjectListMap( art, replaceFileObjects );
  }
}
