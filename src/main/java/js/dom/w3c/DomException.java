package js.dom.w3c;

/**
 * Document object model package exception.
 * 
 * @author Iulian Rotaru
 */
public class DomException extends RuntimeException
{
  private static final long serialVersionUID = 3072256185667976431L;

  public DomException()
  {
    super();
  }

  public DomException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public DomException(String message)
  {
    super(message);
  }

  public DomException(Throwable cause)
  {
    super(cause);
  }

  public DomException(String message, Object... args)
  {
    super(String.format(message, args));
  }
}
