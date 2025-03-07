//////////////////////////////////////////////////////////////////////////////
// 
//                    Copyright 2025, Cornutum Project
//                             www.cornutum.org
//
//////////////////////////////////////////////////////////////////////////////

package org.cornutum.tcases.openapi.testwriter;

import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Identifies a {@link TestWriter} implementation
 */
@Documented
@Retention( RUNTIME)
public @interface ApiTestWriter
  {
  /**
   * Defines the name used to locate this {@link TestWriter} implementation at runtime
   */
  String name();

  /**
   * Defines the name used to locate the {@link TestTarget} implementation required for this {@link TestWriter}.
   * If omitted, the default is {@link JavaTestTarget}.
   */
  String target() default "java";
  }
