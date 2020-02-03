package org.hitachi.vantara.builder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.hitachi.vantara.api.ISelectorBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Dmitriy Stepanov on 10/11/17.
 */
public class NoArtifactSelectorBuilder implements ISelectorBuilder {

  public Map<FileObject, List<FileObject>> findObjectsToReplace( Map<FileObject, List<FileObject>> replace, FileObject outputDir ) throws FileSystemException {
    return Collections.emptyMap();
  }
}
