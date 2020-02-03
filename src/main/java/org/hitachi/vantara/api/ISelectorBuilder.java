package org.hitachi.vantara.api;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.util.List;
import java.util.Map;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public interface ISelectorBuilder {

  Map<FileObject, List<FileObject>> findObjectsToReplace( Map<FileObject, List<FileObject>> replace, FileObject outputDir ) throws FileSystemException;

}
