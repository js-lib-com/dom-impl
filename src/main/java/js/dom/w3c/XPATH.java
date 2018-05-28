package js.dom.w3c;

/**
 * Constant XPath expressions used internally by DOM package.
 * 
 * @author Iulian Rotaru
 */
final class XPATH
{
  /** Select descendants with requested CSS class name. */
  static final String CSS_CLASS = "descendant-or-self::*[contains(concat(' ', normalize-space(@class), ' '), ' %s ')]";

  /**
   * Forbid default constructor synthesis.
   */
  private XPATH()
  {
  }
}
