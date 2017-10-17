package org.hitachi.vantara;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.maven.artifact.Artifact;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public class Utils {

  public static void unpack( FileObject outputDir, FileObject packFileObject ) throws FileSystemException {
    outputDir.createFolder();

    try {
      final FileObject zipFileSystem = VFS.getManager().createFileSystem( packFileObject );
      try {
        outputDir.copyFrom( zipFileSystem, new AllFileSelector() );
      } finally {
        zipFileSystem.close();
      }
    } finally {
      packFileObject.close();
    }
  }

  public static void pack( FileObject inputDir, FileObject packFileObject ) throws FileSystemException {
    packFileObject.createFile();
    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream( packFileObject.getContent().getOutputStream() );
      addToZip( inputDir, zos );
    } finally {
      try {
        if ( zos != null ) {
          zos.close();
        }
      } catch ( IOException e ) {
        throw new FileSystemException( e );
      }
      packFileObject.close();
    }
  }

  private static void addToZip( FileObject file, ZipOutputStream zos ) throws FileSystemException {

    // Locate the files to copy across
    final ArrayList<FileObject> files = new ArrayList<FileObject>();
    file.findFiles( new AllFileSelector(), false, files );
    byte[] buf = new byte[ 1024 ];

    try {
      // Copy everything across
      for ( final FileObject srcFile : files ) {
        // Determine the destination file
        final String relPath = file.getName().getRelativeName( srcFile.getName() );
        ZipEntry zipEntry = new ZipEntry( relPath );

        if ( srcFile.getType().hasContent() ) {
          zos.putNextEntry( zipEntry );
          InputStream is = null;
          try {
            is = srcFile.getContent().getInputStream();
            // Write to zip.
            for ( int readNum; ( readNum = is.read( buf ) ) != -1; ) {
              zos.write( buf, 0, readNum );
            }
          } catch ( IOException e ) {
            throw new FileSystemException( e );
          } finally {
            if ( is != null ) {
              is.close();
            }
          }
          zos.closeEntry();
        } else if ( srcFile.getType().hasChildren() ) {
          //zos.closeEntry();
        }
      }
    } catch ( IOException e ) {
      throw new FileSystemException( e );
    }
  }


  public static FileSelector getSimpleFileSelector( final String toFind, final boolean directory ) {
    return new FileSelector() {
      public boolean includeFile( FileSelectInfo fileInfo ) throws Exception {
        FileObject fo = fileInfo.getFile();
        String baseName = fo.getName().getBaseName();
        if ( directory ) {
          return baseName.equals( toFind );
        } else {
          //todo into zip
          return baseName.equals( toFind );
        }
      }

      public boolean traverseDescendents( FileSelectInfo fileInfo ) throws Exception {
        return true;
      }
    };
  }

  public static String generateFileNameFromArtifact( Artifact artifact ) {
    String classifier = artifact.getClassifier();
    String type = artifact.getType();
    if ( type.equals( "java-source" ) ) {
      type = "jar";
    }
    if ( classifier == null || classifier.isEmpty() ) {
      return String.format( "%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), type );
    } else {
      return String.format( "%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), classifier,
        type );
    }
  }
}
