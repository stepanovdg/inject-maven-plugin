package org.hitachi.vantara.builder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.maven.artifact.Artifact;
import org.hitachi.vantara.api.ISelectorBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hitachi.vantara.Utils.generateFileNameFromArtifact;
import static org.hitachi.vantara.Utils.getSimpleFileSelector;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public class SingleArtifactSelectorBuilder implements ISelectorBuilder {


  protected FileSystemManager fsManager;
  protected Artifact producedArtifact;

  public SingleArtifactSelectorBuilder( FileSystemManager fsManager, Artifact producedArtifact ) {
    this.fsManager = fsManager;
    this.producedArtifact = producedArtifact;
  }

  public Map<FileObject, List<FileObject>> findObjectsToReplace( Map<FileObject, List<FileObject>> ret,
                                                                 FileObject outputDir ) throws FileSystemException {
    File artFile = producedArtifact.getFile();
    if ( artFile != null ) {
      FileObject art = fsManager.toFileObject( artFile );
      return processArtifact( outputDir, ret, art );
    }
    return Collections.emptyMap();
  }

  protected Map<FileObject, List<FileObject>> processArtifact( FileObject outputDir,
                                                               Map<FileObject, List<FileObject>> ret, FileObject key )
    throws FileSystemException {
    String fileArt = key.getName().getBaseName();
    FileObject[] replaceObjects = outputDir.findFiles( getSimpleFileSelector( fileArt, key.isFolder() ) );
    List<FileObject> toReplace = new ArrayList<FileObject>( Arrays.asList( replaceObjects ) );
    String fileNameFromArt = generateFileNameFromArtifact( producedArtifact );
    if ( !fileNameFromArt.equals( fileArt ) ) {
      replaceObjects = outputDir.findFiles( getSimpleFileSelector( fileNameFromArt, key.isFolder() ) );
      toReplace.addAll( Arrays.asList( replaceObjects ) );
    }
    putUpdate( ret, key, toReplace );
    return getFileObjectListMap( key, toReplace );
  }

  protected Map<FileObject, List<FileObject>> getFileObjectListMap( FileObject key, List<FileObject> toReplace ) {
    Map<FileObject, List<FileObject>> retur = null;
    if ( !toReplace.isEmpty() ) {
      retur = new HashMap<FileObject, List<FileObject>>();
      retur.put( key, toReplace );
      return retur;
    } else {
      return Collections.emptyMap();
    }
  }

  protected void putUpdate( Map<FileObject, List<FileObject>> ret, FileObject key, List<FileObject> toReplace ) {
    List<FileObject> value;
    if ( toReplace.isEmpty() ) {
      return;
    }

    if ( ( value = ret.put( key, toReplace ) ) != null ) {
      toReplace.addAll( value );
      ret.put( key, toReplace );
    }
  }


}
