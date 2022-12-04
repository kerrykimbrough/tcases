//////////////////////////////////////////////////////////////////////////////
// 
//                    Copyright 2022, Cornutum Project
//                             www.cornutum.org
//
//////////////////////////////////////////////////////////////////////////////

package org.cornutum.tcases.resolve;

import org.cornutum.tcases.util.ContextHandler;
import org.cornutum.tcases.util.ExecutionNotifier;
import static org.cornutum.tcases.resolve.DataValues.*;

import org.cornutum.regexpgen.RegExpGen;
import org.cornutum.regexpgen.js.Provider;
import static org.cornutum.regexpgen.Bounds.bounded;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides transformations and analysis of {@link Schema} instances.
 */
public class Schemas extends ContextHandler<ExecutionNotifier<?>>
  {
  /**
   * Creates a new Schemas instance.
   */
  public Schemas( ExecutionNotifier<?> context)
    {
    super( context);
    }

  /**
   * Creates a new schema by merging the contents of the given schema with the given default schema.
   */
  public static Schema merge( Schema defaults, Schema schema)
    {
    return
      defaults != null && schema != null?
      defaults.merge( schema) :

      schema != null?
      new Schema( schema) :

      defaults != null?
      new Schema( defaults) :

      null;
    }

  /**
   * Updates the given {@link Schema} to normalize property values.
   */
  public Schema normalize( Schema schema)
    {
    if( !(schema == null || schema.getConstant() != null))
      {
      switch( schema.getType())
        {
        case ARRAY:
          {
          schema.setMaxItems(
            Optional.ofNullable( schema.getMaxItems())
            .filter( max -> max >= 0)
            .orElse( null));
          
          schema.setMinItems(
            Optional.ofNullable( schema.getMinItems())
            .filter( min -> min >= 0)
            .map( min -> Optional.ofNullable( schema.getMaxItems()).map( max -> adjustedMinOf( "Items", min, max)).orElse( min))
            .orElse( null));

          schema.setItems(
            Optional.ofNullable( schema.getItems())
            .map( items -> resultFor( "items", () -> normalize( items)))
            .orElse( null));
          break;
          }

        case INTEGER:
        case NUMBER:
          {
          normalizeNumberRange( schema);
          break;
          }

        case STRING:
          {
          schema.setPattern(
            Optional.ofNullable( schema.getPattern())
            .filter( pattern -> isPatternApplicable( schema.getFormat()))
            .filter( this::isPatternValid)
            .orElse( null));

          normalizeStringLength( schema);
          break;
          }

        default:
          {
          break;
          }
        }
      }
    
    return schema;
    }

  /**
   * Updates the given numeric {@link Schema} to define the effective minimum and maximum (inclusive) values.
   */
  private Schema normalizeNumberRange( Schema schema)
    {
    BigDecimal unit = unitOf( schema);

    schema.setMultipleOf(
      Optional.ofNullable( schema.getMultipleOf())
      .filter( m -> m.compareTo( BigDecimal.ZERO) != 0)
      .orElse( null));
      
    BigDecimal effMultipleOf =
      Optional.ofNullable( schema.getMultipleOf())
      .orElse( unit);
    
    BigDecimal effMin =
      Optional.ofNullable(
        Optional.ofNullable( schema.getExclusiveMinimum())
        .map( xm -> xm.add( effMultipleOf))
        .map( xm -> Optional.ofNullable( schema.getMinimum()).filter( m -> m.compareTo( xm) >= 0).orElse( xm))
        .orElse( schema.getMinimum()))

      .map( min -> multipleAbove( min, effMultipleOf))
      .orElse( null);


    BigDecimal effMax =
      Optional.ofNullable(
        Optional.ofNullable( schema.getExclusiveMaximum())
        .map( xm -> xm.subtract( effMultipleOf))
        .map( xm -> Optional.ofNullable( schema.getMaximum()).filter( m -> m.compareTo( xm) <= 0).orElse( xm))
        .orElse( schema.getMaximum()))

      .map( max -> multipleBelow( max, effMultipleOf))
      .orElse( null);

    schema.setMinimum(
      Optional.ofNullable( effMin)
      .map( min -> Optional.ofNullable( effMax).map( max -> adjustedMinOf( "imum", min, max)).orElse( min))
      .orElse( null));
    schema.setMaximum( effMax);

    schema.setExclusiveMinimum( null);
    schema.setExclusiveMaximum( null);

    return schema;
    }

  /**
   * Updates the given string {@link Schema} to define the effective minimum and maximum (inclusive) length.
   */
  private Schema normalizeStringLength( Schema schema)
    {
    Integer[] lengthRequired = withLengthRequired( schema); 
    Integer minRequired = lengthRequired[0];
    Integer maxRequired = lengthRequired[1];

    Optional.ofNullable( minRequired)
      .flatMap( min -> Optional.ofNullable( maxRequired))
      .filter( max -> max < minRequired)
      .ifPresent( infeasible -> {
        notifyError(
          String.format( "Required length for pattern='%s' is incompatible with format='%s'", schema.getPattern(), schema.getFormat()),
          "Ignoring the pattern for this schema");
        schema.setPattern( null);
        }); 

    schema.setMaxLength(
      Optional.ofNullable( schema.getMaxLength())
      .filter( max -> max >= 0)
      .map( max -> adjustToRange( "maxLength", max, minRequired, maxRequired))
      .orElse( maxRequired));

    schema.setMinLength(
      Optional.ofNullable( schema.getMinLength())
      .filter( min -> min >= 0)
      .map( min -> adjustToRange( "minLength", min, minRequired, maxRequired))
      .map( min -> Optional.ofNullable( schema.getMaxLength()).map( max -> adjustedMinOf( "Length", min, max)).orElse( min))
      .orElse( minRequired));

    return schema;
    }

  /**
   * Returns the adjusted minimum of the given range.
   */
  private <T extends Comparable<T>> T adjustedMinOf( String description, T min, T max)
    {
    if( min.compareTo( max) > 0)
      {
      notifyError(
        String.format(
          "min%s=%s is greater than max%s=%s",
          description, min,
          description, max),

        String.format(
          "Adjusting min%s to max%s",
          description,
          description));

      min = max;
      }

    return min;
    }

  /**
   * Adjusts the given value to lie within the given range.
   */
  private <T extends Comparable<T>> T adjustToRange( String description, T value, T min, T max)
    {
    if( max != null && value.compareTo( max) > 0)
      {
      notifyError(
        String.format( "%s=%s is greater than the required maximum=%s", description, value, max),
        String.format( "Adjusting %s to the required maximum", description));

      value = max;
      }

    if( min != null && value.compareTo( min) < 0)
      {
      notifyError(
        String.format( "%s=%s is less than the required minimum=%s", description, value, min),
        String.format( "Adjusting %s to the required minimum", description));

      value = min;
      }

    return value;
    }

  /**
   * Returns the maximum length required for pattern matches.
   */
  public static Integer maxPatternMatch( Schema schema)
    {
    return
      Optional.ofNullable( schema.getPattern())
      .flatMap( pattern -> Optional.ofNullable( maxPatternMatch( Provider.forEcmaScript().matching( pattern))))
      .orElse( null);
    }

  /**
   * Returns the maximum length required for the given pattern generator.
   */
  public static Integer maxPatternMatch( RegExpGen patternGen)
    {
    return bounded( patternGen.getMaxLength()).orElse( null);
    }

  /**
   * Returns the minimum length required for pattern matches.
   */
  public static Integer minPatternMatch( Schema schema)
    {
    return
      Optional.ofNullable( schema.getPattern())
      .map( pattern -> minPatternMatch( Provider.forEcmaScript().matching( pattern)))
      .orElse( null);
    }

  /**
   * Returns the minimum length required for the given pattern generator.
   */
  public static Integer minPatternMatch( RegExpGen patternGen)
    {
    return patternGen.getMinLength();
    }

  /**
   * Returns true if pattern matching is supported for the given string format.
   */
  private boolean isPatternApplicable( String format)
    {
    boolean applicable = !isPatternedFormat( format);
    if( !applicable)
      {
      notifyWarning( String.format( "Pattern matching not supported for strings with format=%s. Ignoring the pattern for this schema", format));
      }
    return applicable;
    }

  /**
   * Returns true if the given pattern is valid
   */
  private boolean isPatternValid( String pattern)
    {
    try
      {
      Provider.forEcmaScript().matching( pattern);
      return true;
      }
    catch( IllegalArgumentException e)
      {
      notifyError( String.format( "Invalid pattern: %s", e.getMessage()), "Ignoring the pattern for this schema");
      return false;
      }
    }

  /**
   * Returns an array of the form [minLength, maxLength] that defines the string length required by the given schema.
   */
  private Integer[] withLengthRequired( Schema schema)
    {
    Integer minRequired = minLengthRequired( schema);
    Integer maxRequired = maxLengthRequired( schema);
    if( minRequired != null && maxRequired != null && minRequired > maxRequired)
      {
      notifyError(
        String.format( "Required length for pattern='%s' is incompatible with format='%s'", schema.getPattern(), schema.getFormat()),
        "Ignoring the pattern for this schema");

      schema.setPattern( null);
      minRequired = stringFormatMin( schema.getFormat());
      maxRequired = stringFormatMax( schema.getFormat());
      }

    return new Integer[]{ minRequired, maxRequired};
    }

  /**
   * Returns the minimum string length required by the given schema.
   */
  public static Integer minLengthRequired( Schema schema)
    {
    Integer formatMin = stringFormatMin( schema.getFormat());
    Integer patternMin = minPatternMatch( schema);

    return
      Optional.ofNullable( formatMin)
      .map( fm -> Optional.ofNullable( patternMin).map( pm -> Math.max( fm, pm)).orElse( fm))
      .orElse( patternMin);
    }

  /**
   * Returns the maximum string length required by the given schema.
   */
  public static Integer maxLengthRequired( Schema schema)
    {
    Integer formatMax = stringFormatMax( schema.getFormat());
    Integer patternMax = maxPatternMatch( schema);

    return
      Optional.ofNullable( formatMax)
      .map( fm -> Optional.ofNullable( patternMax).map( pm -> Math.min( fm, pm)).orElse( fm))
      .orElse( patternMax);
    }

  /**
   * Returns the unit value for the number range defined by the given schema.
   */
  public static BigDecimal unitOf( Schema schema)
    {
    return
      new BigDecimal(
        BigInteger.ONE,
        Stream.of(
          Optional.ofNullable( schema.getMultipleOf()).map( BigDecimal::scale).orElse( 0),
          Optional.ofNullable( schema.getMinimum()).map( BigDecimal::scale).orElse( 0),
          Optional.ofNullable( schema.getMaximum()).map( BigDecimal::scale).orElse( 0),
          Optional.ofNullable( schema.getExclusiveMinimum()).map( BigDecimal::scale).orElse( 0),
          Optional.ofNullable( schema.getExclusiveMaximum()).map( BigDecimal::scale).orElse( 0))
        .max( Integer::compareTo)
        .orElse( 0));
    }
  }
