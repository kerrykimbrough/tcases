//////////////////////////////////////////////////////////////////////////////
// 
//                    Copyright 2019, Cornutum Project
//                             www.cornutum.org
//
//////////////////////////////////////////////////////////////////////////////

package org.cornutum.tcases.openapi.resolver;

import org.cornutum.tcases.util.ToString;
import static org.cornutum.tcases.util.CollectionUtils.toStream;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;

/**
 * Describes an abstract test case for an API request.
 */
public class RequestCaseDef
  {
  /**
   * Creates a new RequestCaseDef instance.
   */
  public RequestCaseDef()
    {
    setParams( null);
    }

  /**
   * Changes the server URI for this request.
   */
  public void setServer( URI uri)
    {
    server_ = uri;
    }

  /**
   * Returns the server URI for this request.
   */
  public URI getServer()
    {
    return server_;
    }

  /**
   * Changes the API version for this request.
   */
  public void setVersion( String version)
    {
    version_ = version;
    }

  /**
   * Returns the API version for this request.
   */
  public String getVersion()
    {
    return version_;
    }

  /**
   * Changes the path for this request.
   */
  public void setPath( String path)
    {
    path_ = path;
    }

  /**
   * Returns the path for this request.
   */
  public String getPath()
    {
    return path_;
    }

  /**
   * Changes the operation for this request.
   */
  public void setOperation( String operation)
    {
    op_ = operation;
    }

  /**
   * Returns the operation for this request.
   */
  public String getOperation()
    {
    return op_;
    }

  /**
   * Changes the parameter definitions for this request.
   */
  public void setParams( Iterable<ParamDef> params)
    {
    params_ =
      toStream( Optional.ofNullable( params).orElse( Collections.emptyList()))
      .collect( toList());
    }

  /**
   * Returns the parameter definitions for this request.
   */
  public Iterable<ParamDef> getParams()
    {
    return params_;
    }

  /**
   * Adds a parameter definition for this request.
   */
  public void addParam( ParamDef param)
    {
    params_.add( param);
    }

  /**
   * Changes the definition of the request body.
   */
  public void setBody( ValueDef<?> body)
    {
    body_ = body;
    }

  /**
   * Returns the definition of the request body.
   */
  public ValueDef<?> getBody()
    {
    return body_;
    }

  /**
   * Changes the name of the invalid input for this request.
   */
  public void setInvalidVar( String invalidVar)
    {
    invalidVar_ = invalidVar;
    }

  /**
   * Returns the name of the invalid input for this request. Returns null for a valid request.
   */
  public String getInvalidVar()
    {
    return invalidVar_;
    }

  /**
   * Returns true for a request with an invalid input.
   */
  public boolean isFailure()
    {
    return getInvalidVar() != null;
    }

  public String toString()
    {
    return
      ToString.getBuilder( this)
      .append( getOperation())
      .append( getPath())
      .append( isFailure()? "FAILURE" : "SUCCESS")
      .toString();
    }

  private URI server_;
  private String version_;
  private String path_;
  private String op_;
  private List<ParamDef> params_;
  private ValueDef<?> body_;
  private String invalidVar_;
  }
