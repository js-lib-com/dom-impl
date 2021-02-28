package js.dom.w3c;

import js.dom.Attr;
import js.util.Strings;

/**
 * Immutable element attribute implementation.
 * 
 * @author Iulian Rotaru
 */
final class AttrImpl implements Attr
{
  private final String namespaceURI;
  private final String name;
  private final String value;

  public AttrImpl(String name, String value)
  {
    this.namespaceURI = null;
    this.name = name;
    this.value = value;
  }

  public AttrImpl(String namespaceURI, String name, String value)
  {
    this.namespaceURI = namespaceURI;
    this.name = name;
    this.value = value;
  }

  @Override
  public String getNamespaceURI()
  {
    return namespaceURI;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getValue()
  {
    return value;
  }

  @Override
  public String toString()
  {
    return Strings.toString(name, value);
  }

  /**
   * Cache hash code for this immutable instance.
   */
  private int hash;

  @Override
  public int hashCode()
  {
    if(hash == 0) {
      final int prime = 31;
      hash = prime + ((name == null) ? 0 : name.hashCode());
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    AttrImpl other = (AttrImpl)obj;
    if(name == null) {
      if(other.name != null) return false;
    }
    else if(!name.equals(other.name)) return false;
    return true;
  }
}
