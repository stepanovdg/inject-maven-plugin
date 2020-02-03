package org.hitachi.vantara;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.hitachi.vantara.api.ISelectorBuilder;
import org.hitachi.vantara.builder.NoArtifactSelectorBuilder;
import org.hitachi.vantara.builder.SingleArtifactSelectorBuilder;
import org.hitachi.vantara.builder.YAMLMappingSelectorBuilder;
import org.hitachi.vantara.builder.ZipArtifactSelectorBuilder;
import org.hitachi.vantara.mapping.ProjectMapping;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.hitachi.vantara.InjectMojo.PLUGIN_LOG_PREFIX;

/**
 * Created by Dmitriy Stepanov on 9/21/17.
 */
public class SelectorBuilderFactory {
  private static final String MAPPING_CE_YAML = "artifactMapping-ce.yaml";
  private static final String MAPPING_EE_YAML = "artifactMapping-ee.yaml";

  FileSystemManager fsManager = null;
  private Log log;
  private MavenProject project;
  private FileObject targetDir;
  private List<ProjectMapping> projectMappings;

  public SelectorBuilderFactory( Log log, MavenProject project, URL overideMappingFile ) throws IOException {
    this.log = log;
    this.project = project;
    fsManager = VFS.getManager();
    targetDir = fsManager.resolveFile( project.getBasedir(), "target" );
    projectMappings = new ArrayList<ProjectMapping>();
    InputStream yamlIs = null;
    if ( overideMappingFile != null ) {
      try {
        log.debug( PLUGIN_LOG_PREFIX + " Used mapping from override file" );
        yamlIs = fsManager.resolveFile( overideMappingFile ).getContent().getInputStream();
        if ( yamlIs != null ) {
          ProjectMapping mapping = getProjectMapping( yamlIs );
          projectMappings.add( mapping );
        }
      } catch ( FileSystemException e ) {
        log.debug( PLUGIN_LOG_PREFIX + " Failed to get input stream from override mapping file" );
        log.debug( e );
      }
    }

    yamlIs = getClass().getResourceAsStream( MAPPING_EE_YAML );
    if ( yamlIs != null ) {
      projectMappings.add( getProjectMapping( yamlIs ) );
    }

    yamlIs = getClass().getResourceAsStream( MAPPING_CE_YAML );
    if ( yamlIs != null ) {
      projectMappings.add( getProjectMapping( yamlIs ) );
    }
  }

  private ProjectMapping getProjectMapping( InputStream yamlIs ) {
    ObjectMapper mapper = new ObjectMapper( new YAMLFactory() );
    mapper.configure( SerializationFeature.WRAP_ROOT_VALUE, true );
    try {
      return mapper.readValue( yamlIs, ProjectMapping.class );
    } catch ( IOException e ) {
      log.error( e );
    }
    return null;
  }

  public ISelectorBuilder createFileSelectorBuilder( Artifact producedArtifact ) {
    String type = producedArtifact.getType();

    if ( !projectMappings.isEmpty() ) {
      YAMLMappingSelectorBuilder builder =
        new YAMLMappingSelectorBuilder( fsManager, producedArtifact, targetDir, project );
      try {
        boolean res = false;
        for ( ProjectMapping is : projectMappings ) {
          if ( is == null ){
            continue;
          }
          res = builder.init( is );
          if ( res ) {
            break;
          }
        }
        if ( res ) {
          log.info( PLUGIN_LOG_PREFIX + "YAMLBuilder" );
          return builder;
        } else {
          throw new IOException( "Failed to find mapping for that artifact try to guess what to replace" );
        }
      } catch ( IOException e ) {
        log.debug( PLUGIN_LOG_PREFIX + " Failed to create YAMLBuilder" );
        log.debug( e );
      }
    }

    if ( "bundle".equals( type ) ) {
      log.info( PLUGIN_LOG_PREFIX + "SingleBuilder for bundle" );

      return new SingleArtifactSelectorBuilder( fsManager, producedArtifact );
    }

    if ( "pom".equals( type ) ) {
      log.info( PLUGIN_LOG_PREFIX + "NoArtifactBuilder for pom" );

      return new NoArtifactSelectorBuilder();
    }

    if ( "zip".equals( type ) ) { //TODO add other checks like maven assemble plugin in plugin map
      log.info( PLUGIN_LOG_PREFIX + "ZipBuilder" );

      return new ZipArtifactSelectorBuilder( fsManager, producedArtifact, targetDir );
    }
    log.info( PLUGIN_LOG_PREFIX + "SingleBuilder" );

    return new SingleArtifactSelectorBuilder( fsManager, producedArtifact );

  }

}
