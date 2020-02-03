package org.hitachi.vantara;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.hitachi.vantara.api.ISelectorBuilder;
import org.hitachi.vantara.builder.NoArtifactSelectorBuilder;
import org.hitachi.vantara.builder.YAMLMappingSelectorBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hitachi.vantara.Utils.pack;


/**
 * Created by Dmitriy Stepanov on 9/21/17.
 */

@Mojo( name = "inject" )
public class InjectMojo extends AbstractMojo {

  public static final String PLUGIN_LOG_PREFIX = "INJECT-MAVEN-PLUGIN:";

  @Parameter( property = "inject.plugin.validate.mode", defaultValue = "false" )
  private Boolean validateModeOn;

  @Parameter( property = "vantara.install.dir" )
  private URL outputDirectory;

  @Parameter( property = "inject.plugin.mapping.ovverride.file" )
  private URL overideMappingFile;

  @Parameter( defaultValue = "${project}", required = true, readonly = true )
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    FileSystemManager fsManager = null;
    FileObject output = null;

    if ( outputDirectory == null ) {
      return;
    }

    try {
      fsManager = VFS.getManager();
      getLog().info( PLUGIN_LOG_PREFIX + " Install directory property=" + outputDirectory );
      output = fsManager.resolveFile( outputDirectory );

      SelectorBuilderFactory factory = new SelectorBuilderFactory( getLog(), project, overideMappingFile );

      ISelectorBuilder builder = null;
      Map<FileObject, List<FileObject>> toReplace = null;
      List<Artifact> arts = new ArrayList<Artifact>();
      Map<FileObject, List<FileObject>> ret = new HashMap<FileObject, List<FileObject>>();

      Artifact mainArtifact = project.getArtifact();
      arts.add( mainArtifact );
      arts.addAll( project.getAttachedArtifacts() );

      for ( Artifact artifact : arts ) {

        builder = factory.createFileSelectorBuilder( artifact );
        toReplace = builder.findObjectsToReplace( ret, output );
        if ( validateModeOn && toReplace.isEmpty() && !( builder instanceof NoArtifactSelectorBuilder
          || builder instanceof YAMLMappingSelectorBuilder ) && (
          artifact.getClassifier() == null || !( artifact.getClassifier().equals( "sources" ) ) && !( artifact
            .getClassifier().equals( "tests" ) ) ) ) {
          throw new MojoExecutionException(
            "No files to replace found something is not working - if it is correct add mapping to yaml file. Artifact="
              + artifact );
        }
        if ( builder instanceof YAMLMappingSelectorBuilder ) {
          break;
        }
      }

      replaceArtifact( ret );
    } catch ( FileSystemException e ) {
      getLog().error( e );
    } catch ( IOException e ) {
      getLog().error( e );
    }

  }

  private void replaceArtifact( Map<FileObject, List<FileObject>> toReplace ) throws MojoExecutionException {
    //TODO make plan to not replace twice
    //TODO make tasks - to do in parralel
    for ( FileObject art : toReplace.keySet() ) {
      getLog().info( PLUGIN_LOG_PREFIX + "  replace=" + art );//TODO delete
      getLog().info( PLUGIN_LOG_PREFIX + "  to replace=" + toReplace.get( art ) );//TODO delete
      replaceArtifact( art, toReplace.get( art ) );
    }
  }

  private void replaceArtifact( FileObject artifact, List<FileObject> replaceObjects ) throws MojoExecutionException {
    if ( artifact == null ) {
      return;
    }

    for ( FileObject repl : replaceObjects ) {
      try {
        FileObject folder = repl.getParent();
        if ( !artifact.isFolder() ) {
          getLog().info( PLUGIN_LOG_PREFIX + " Artifact is fiel" + artifact );//todo delete

          if ( !folder.exists() ) {
            folder.createFolder();
          } else {
            //BACKUP
            getLog().info( PLUGIN_LOG_PREFIX + " start backup" + artifact );//todo delete

            String childName = repl.getName().getBaseName();
            FileObject backedUpArt = folder.resolveFile( childName + "_back" );
            getLog().info( PLUGIN_LOG_PREFIX + " backing to " + backedUpArt );//todo delete

            if ( repl.exists() && !backedUpArt.exists() ) {
              getLog().info( PLUGIN_LOG_PREFIX + " backed to " + backedUpArt );//todo delete
              repl.moveTo( backedUpArt );
            }
            //delete
            repl.deleteAll();
            getLog().info( PLUGIN_LOG_PREFIX + " deleted previous to " + repl );//todo delete

          }

          repl.copyFrom( artifact, Selectors.SELECT_SELF );

          //IOUtils.copy( artifact.getContent().getInputStream(), repl.getContent().getOutputStream( false ) );
          getLog().info( PLUGIN_LOG_PREFIX + " Copied " + artifact.getURL() + " to " + repl.getURL() );
        } else {
          if ( !repl.exists() ) {
            repl.createFolder();
          } else {
            //BACKUP zipping
            FileObject backedUpZip = folder.resolveFile( repl.getName().getBaseName() + ".zip" );
            if ( !backedUpZip.exists() ) {
              pack( repl, backedUpZip );
            }
            //delete before replacing
            repl.deleteAll();
          }
          repl.copyFrom( artifact, new AllFileSelector() );
          getLog().info( PLUGIN_LOG_PREFIX + " Copied " + artifact.getURL() + " to " + repl.getURL() );
        }
      } catch ( FileSystemException e ) {
        e.printStackTrace();
        return;
      }
    }
  }
}


