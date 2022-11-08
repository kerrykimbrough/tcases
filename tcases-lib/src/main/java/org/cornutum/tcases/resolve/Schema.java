//////////////////////////////////////////////////////////////////////////////
// 
//                    Copyright 2022, Cornutum Project
//                             www.cornutum.org
//
//////////////////////////////////////////////////////////////////////////////

package org.cornutum.tcases.resolve;

import org.cornutum.tcases.resolve.DataValue.Type;
import org.cornutum.tcases.util.ToString;

import static org.cornutum.tcases.resolve.DataValue.Type.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines requirements for the value of a system input variable.
 */
public class Schema
  {  
  /**
   * Creates a new Schema instance of the given type.
   */
  public Schema( Type type)
    {
    assertType( type);
    type_ = type;
    }
  
  /**
   * Creates a new copy of another Schema instance.
   */
  public Schema( Schema other)
    {
    this( Optional.ofNullable( other).map( Schema::getType).orElse( null));
    setConstant( other.getConstant());
    setFormat( other.getFormat());
    setMinimum( other.getMinimum());
    setMaximum( other.getMaximum());
    setExclusiveMinimum( other.getExclusiveMinimum());
    setExclusiveMaximum( other.getExclusiveMaximum());
    setMultipleOf( other.getMultipleOf());
    setMinLength( other.getMinLength());
    setMaxLength( other.getMaxLength());
    setPattern( other.getPattern());
    setMinItems( other.getMinItems());
    setMaxItems( other.getMaxItems());
    setUniqueItems( other.getUniqueItems());
    setItems( Optional.ofNullable( other.getItems()).map( Schema::new).orElse( null));
    }

  /**
   * Returns the type of input values.
   */
  public Type getType()
    {
    return type_;
    }

  /**
   * Changes the constant value of all input values.
   */
  public void setConstant( DataValue<?> constant)
    {
    assertValueType( "const", constant);
    constant_ = constant;
    }

  /**
   * Returns the constant value of all input values.
   */
  public DataValue<?> getConstant()
    {
    return constant_;
    }

  /**
   * Changes the format of input values.
   */
  public void setFormat( String format)
    {
    format_ = format;
    }

  /**
   * Returns the format of input values.
   */
  public String getFormat()
    {
    return format_;
    }

  /**
   * Changes the minimum (inclusive) for numeric values.
   */
  public void setMinimum( BigDecimal minimum)
    {
    assertRequiredType( "minimum", NUMBER, INTEGER);
    minimum_ = minimum;
    }

  /**
   * Returns the minimum (inclusive) for numeric values.
   */
  public BigDecimal getMinimum()
    {
    return minimum_;
    }

  /**
   * Changes the maximum (inclusive) for numeric values.
   */
  public void setMaximum( BigDecimal maximum)
    {
    assertRequiredType( "maximum", NUMBER, INTEGER);
    maximum_ = maximum;
    }

  /**
   * Returns the maximum (inclusive) for numeric values.
   */
  public BigDecimal getMaximum()
    {
    return maximum_;
    }

  /**
   * Changes the minimum (exclusive) for numeric values.
   */
  public void setExclusiveMinimum( BigDecimal exclusiveMinimum)
    {
    assertRequiredType( "exclusiveMinimum", NUMBER, INTEGER);
    exclusiveMinimum_ = exclusiveMinimum;
    }

  /**
   * Returns the minimum (exclusive) for numeric values.
   */
  public BigDecimal getExclusiveMinimum()
    {
    return exclusiveMinimum_;
    }

  /**
   * Changes the maximum (exclusive) for numeric values.
   */
  public void setExclusiveMaximum( BigDecimal exclusiveMaximum)
    {
    assertRequiredType( "exclusiveMaximum", NUMBER, INTEGER);
    exclusiveMaximum_ = exclusiveMaximum;
    }

  /**
   * Returns the maximum (exclusive) for numeric values.
   */
  public BigDecimal getExclusiveMaximum()
    {
    return exclusiveMaximum_;
    }

  /**
   * Changes the common divisor for numeric input values.
   */
  public void setMultipleOf( BigDecimal multipleOf)
    {
    assertRequiredType( "multipleOf", NUMBER, INTEGER);
    multipleOf_ = multipleOf;
    }

  /**
   * Returns the common divisor for numeric input values.
   */
  public BigDecimal getMultipleOf()
    {
    return multipleOf_;
    }

  /**
   * Changes the minimum length of string values.
   */
  public void setMinLength( Integer minLength)
    {
    assertRequiredType( "minLength", STRING);
    minLength_ = minLength;
    }

  /**
   * Returns the minimum length of string values.
   */
  public Integer getMinLength()
    {
    return minLength_;
    }

  /**
   * Changes the maximum length of string values.
   */
  public void setMaxLength( Integer maxLength)
    {
    assertRequiredType( "maxLength", STRING);
    maxLength_ = maxLength;
    }

  /**
   * Returns the maximum length of string values.
   */
  public Integer getMaxLength()
    {
    return maxLength_;
    }

  /**
   * Changes the regular expression matching string values.
   */
  public void setPattern( String pattern)
    {
    assertRequiredType( "pattern", STRING);
    pattern_ = pattern;
    }

  /**
   * Returns the regular expression matching string values.
   */
  public String getPattern()
    {
    return pattern_;
    }

  /**
   * Changes the minimum size of array values.
   */
  public void setMinItems( Integer minItems)
    {
    assertRequiredType( "minItems", ARRAY);
    minItems_ = minItems;
    }

  /**
   * Returns the minimum size of array values.
   */
  public Integer getMinItems()
    {
    return minItems_;
    }

  /**
   * Changes the maximum size of array values.
   */
  public void setMaxItems( Integer maxItems)
    {
    assertRequiredType( "maxItems", ARRAY);
    maxItems_ = maxItems;
    }

  /**
   * Returns the maximum size of array values.
   */
  public Integer getMaxItems()
    {
    return maxItems_;
    }

  /**
   * Changes if array items must be unique.
   */
  public void setUniqueItems( Boolean uniqueItems)
    {
    assertRequiredType( "uniqueItems", ARRAY);
    uniqueItems_ = uniqueItems;
    }

  /**
   * Returns if array items must be unique.
   */
  public Boolean getUniqueItems()
    {
    return uniqueItems_;
    }

  /**
   * Changes the schema for array items.
   */
  public void setItems( Schema items)
    {
    assertRequiredType( "items", ARRAY);
    items_ = items;
    }

  /**
   * Returns the schema for array items.
   */
  public Schema getItems()
    {
    return items_;
    }

  /**
   * Reports a failure if the given type is not defined.
   */
  private void assertType( Type type)
    {
    if( type == null)
      {
      throw new IllegalArgumentException( "No schema type defined");
      }
    }

  /**
   * Reports a failure if this schema does not have one of the types required for the given property.
   */
  private void assertRequiredType( String property, Type... requiredTypes)
    {
    if( Arrays.stream( requiredTypes).noneMatch( type -> type == getType()))
      {
      throw new IllegalArgumentException( String.format( "Property=%s is not allowed for schema type=%s", property, getType()));
      }
    }

  /**
   * Reports a failure if this schema does not have the type required for the given value.
   */
  private void assertValueType( String property, DataValue<?> value)
    {
    Type valueType = Optional.ofNullable( value).map( DataValue::getType).orElse( null);
    if( !(valueType == getType() || valueType == NULL))
      {
      throw new IllegalArgumentException( String.format( "'%s' type=%s is not allowed for schema type=%s", property, getType()));
      }
    }

  @Override
  public String toString()
    {
    return
      ToString.getBuilder( this)
      .append( getType())
      .toString();
    }

  @Override
  public boolean equals( Object object)
    {
    Schema other =
      object != null && object.getClass().equals( getClass())
      ? (Schema) object
      : null;

    return
      other != null
      && Objects.equals( other.getType(), getType())
      && Objects.equals( other.getConstant(), getConstant())
      && Objects.equals( other.getFormat(), getFormat())
      && Objects.equals( other.getMinimum(), getMinimum())
      && Objects.equals( other.getMaximum(), getMaximum())
      && Objects.equals( other.getExclusiveMinimum(), getExclusiveMinimum())
      && Objects.equals( other.getExclusiveMaximum(), getExclusiveMaximum())
      && Objects.equals( other.getMultipleOf(), getMultipleOf())
      && Objects.equals( other.getMinLength(), getMinLength())
      && Objects.equals( other.getMaxLength(), getMaxLength())
      && Objects.equals( other.getPattern(), getPattern())
      && Objects.equals( other.getMinItems(), getMinItems())
      && Objects.equals( other.getMaxItems(), getMaxItems())
      && Objects.equals( other.getUniqueItems(), getUniqueItems())
      && Objects.equals( other.getItems(), getItems())
      ;
    }

  @Override
  public int hashCode()
    {
    return
      getClass().hashCode()
      ^ Objects.hashCode( getType())
      ^ Objects.hashCode( getConstant())
      ^ Objects.hashCode( getFormat())
      ^ Objects.hashCode( getMinimum())
      ^ Objects.hashCode( getMaximum())
      ^ Objects.hashCode( getExclusiveMinimum())
      ^ Objects.hashCode( getExclusiveMaximum())
      ^ Objects.hashCode( getMultipleOf())
      ^ Objects.hashCode( getMinLength())
      ^ Objects.hashCode( getMaxLength())
      ^ Objects.hashCode( getPattern())
      ^ Objects.hashCode( getMinItems())
      ^ Objects.hashCode( getMaxItems())
      ^ Objects.hashCode( getUniqueItems())
      ^ Objects.hashCode( getItems())
      ;
    }
  
  private final Type type_;
  private DataValue<?> constant_;
  private String format_;
  private BigDecimal minimum_;
  private BigDecimal maximum_;
  private BigDecimal exclusiveMinimum_;
  private BigDecimal exclusiveMaximum_;
  private BigDecimal multipleOf_;
  private Integer minLength_;
  private Integer maxLength_;
  private String pattern_;
  private Integer minItems_;
  private Integer maxItems_;
  private Boolean uniqueItems_;
  private Schema items_;
  }

