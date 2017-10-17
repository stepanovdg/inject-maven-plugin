package org.hitachi.vantara.builder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.maven.artifact.Artifact;
import org.hitachi.vantara.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public class ZipArtifactSelectorBuilder extends SingleArtifactSelectorBuilder {

  public static final String ASSEMBLY_ZIP_FORMAT = "%s-%s.zip";
  public static final String ASSEMBLY_UNZIP_FORMAT = "%s-%s_unzipped";
  protected final FileObject targetDir;

  public ZipArtifactSelectorBuilder( FileSystemManager fsManager,
                                     Artifact producedArtifact,
                                     FileObject targetDir ) {
    super( fsManager, producedArtifact );
    this.targetDir = targetDir;
  }

  public Map<FileObject, List<FileObject>> findObjectsToReplace( Map<FileObject, List<FileObject>> ret,
                                                                 FileObject outputDir ) throws FileSystemException {
    FileObject zipAssembly = getZipArtifactLocation( targetDir );
    FileObject unzipAssembly = getZipOutputLocation( targetDir );
    Utils.unpack( unzipAssembly, zipAssembly );

    List<FileObject> zipContent = Arrays.asList( unzipAssembly.getChildren() );

    Map<FileObject, List<FileObject>> retur = new HashMap<FileObject, List<FileObject>>();

    for ( FileObject child : zipContent ) {
      retur.putAll( processArtifact( outputDir, ret, child ) );
    }

    return retur;
  }

  private FileObject getZipOutputLocation( FileObject targetDir ) throws FileSystemException {
    String zipName =
      String.format( ASSEMBLY_UNZIP_FORMAT, producedArtifact.getArtifactId(), producedArtifact.getVersion() );
    FileObject zip = targetDir.resolveFile( zipName );
    zip.createFolder();
    return zip;
  }

  private FileObject getZipArtifactLocation( FileObject targetDir ) throws FileSystemException {
    FileObject zip;
    String zipName =
      String.format( ASSEMBLY_ZIP_FORMAT, producedArtifact.getArtifactId(), producedArtifact.getVersion() );
    zip = targetDir.getChild( zipName );
    if ( zip == null ) {
      for ( FileObject child : Arrays.asList( targetDir.getChildren() ) ) {
        if ( child.getName().getBaseName().startsWith( producedArtifact.getArtifactId() ) ) {
          return child;
        }
      }
    } else {
      return zip;
    }
    throw new FileSystemException( "Wasn`t able to find zip artifact - try to use mapping file for this project" );
  }

}
