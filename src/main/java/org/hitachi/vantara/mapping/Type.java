package org.hitachi.vantara.mapping;

/**
 * Created by Dmitriy Stepanov on 9/27/17.
 */
public enum Type {
  ZIP( "zip:" ), TAR( "tar:" ), GZIP( "gz:" ), TARGZ( "tgz:" ), TABRZ2( "tbz2:" ), JAR( "jar:" ), POM(), FILE();


  public String schema = "";

  Type( String schema ) {
    this.schema = schema;
  }

  Type() {
  }
}
