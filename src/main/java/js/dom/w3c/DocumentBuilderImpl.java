package js.dom.w3c;

import static js.util.Params.isFalse;
import static js.util.Params.notNull;
import static js.util.Params.notNullOrEmpty;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMParser;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;

/**
 * Document object builder. Supply factory methods for documents creation, parsing from string and loading from various
 * sources: file, input stream, input source and URL. There are different factory methods for XML and HTML documents and
 * all are in two flavors: with or without name space support. For name space support this class follows W3C DOM
 * notation convention and uses <code>NS</code> suffix.
 * <p>
 * All loaders use XML declaration or HTML meta Content-Type to choose characters encoding; anyway, loader variant using
 * input source can force a particular encoding.
 * 
 * @author Iulian Rotaru
 */
public final class DocumentBuilderImpl implements DocumentBuilder
{
  /** Class logger. */
  private final static Log log = LogFactory.getLog(DocumentBuilderImpl.class);

  /** XML parser feature for name space support. */
  private static final String FEAT_NAMESPACES = "http://xml.org/sax/features/namespaces";
  /** XML parser feature for schema validation. */
  private static final String FEAT_SCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";
  /** XML parser feature for DOCTYPE disable. */
  private static final String FEAT_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

  @Override
  public EntityResolver getDefaultEntityResolver()
  {
    return new EntityResolverImpl();
  }

  // ----------------------------------------------------
  // create empty XML document

  @Override
  public Document createXML(String root)
  {
    return createXML(root, false);
  }

  @Override
  public Document createXMLNS(String root)
  {
    return createXML(root, true);
  }

  /**
   * Helper method for XML document creation.
   * 
   * @param root name of the root element,
   * @param useNamespace flag to control name space awareness.
   * @return newly create document.
   * @throws IllegalArgumentException if <code>root</code> argument is null or empty.
   */
  private static Document createXML(String root, boolean useNamespace) throws IllegalArgumentException
  {
    notNullOrEmpty(root, "Root element");
    try {
      org.w3c.dom.Document doc = getDocumentBuilder(null, useNamespace).newDocument();
      doc.appendChild(doc.createElement(root));
      return new DocumentImpl(doc);
    }
    catch(Exception e) {
      throw new DomException(e);
    }
  }

  // ----------------------------------------------------
  // parse XML document from string source

  @Override
  public Document parseXML(String string)
  {
    notNullOrEmpty(string, "Source string");
    try {
      return loadXML(new ByteArrayInputStream(string.getBytes("UTF-8")));
    }
    catch(UnsupportedEncodingException e) {
      throw new BugError("JVM with missing support for UTF-8.");
    }
  }

  @Override
  public Document parseXMLNS(String string)
  {
    notNullOrEmpty(string, "Source string");
    try {
      return loadXMLNS(new ByteArrayInputStream(string.getBytes("UTF-8")));
    }
    catch(UnsupportedEncodingException e) {
      throw new BugError("JVM with missing support for UTF-8.");
    }
  }

  // ----------------------------------------------------
  // load XML document from file

  @Override
  public Document loadXML(File file) throws FileNotFoundException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadXML(new FileInputStream(file));
  }

  @Override
  public Document loadXMLNS(File file) throws FileNotFoundException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadXMLNS(new FileInputStream(file));
  }

  // ----------------------------------------------------
  // load XML document from input stream

  @Override
  public Document loadXML(InputStream stream)
  {
    notNull(stream, "Input stream");
    return loadXML(new InputSource(stream));
  }

  @Override
  public Document loadXMLNS(InputStream stream)
  {
    notNull(stream, "Input stream");
    return loadXMLNS(new InputSource(stream));
  }

  // ----------------------------------------------------
  // load XML document from reader

  @Override
  public Document loadXML(Reader reader)
  {
    notNull(reader, "Source reader");
    return loadXML(new InputSource(reader));
  }

  @Override
  public Document loadXMLNS(Reader reader)
  {
    notNull(reader, "Source reader");
    return loadXMLNS(new InputSource(reader));
  }

  // ----------------------------------------------------
  // load XML document from input source

  @Override
  public Document loadXML(InputSource source)
  {
    notNull(source, "Source");
    return loadXML(source, false);
  }

  @Override
  public Document loadXMLNS(InputSource source)
  {
    notNull(source, "Source");
    return loadXML(source, true);
  }

  /**
   * Helper method to load XML document from input source.
   * 
   * @param source input source,
   * @param useNamespace flag to control name space awareness.
   * @return newly created XML document.
   */
  private static Document loadXML(InputSource source, boolean useNamespace)
  {
    try {
      org.w3c.dom.Document doc = getDocumentBuilder(null, useNamespace).parse(source);
      return new DocumentImpl(doc);
    }
    catch(Exception e) {
      throw new DomException(e);
    }
    finally {
      close(source);
    }
  }

  // ----------------------------------------------------
  // load XML document from URL

  @Override
  public Document loadXML(URL url)
  {
    notNull(url, "Source URL");
    return loadXML(url, false);
  }

  @Override
  public Document loadXMLNS(URL url)
  {
    notNull(url, "Source URL");
    return loadXML(url, true);
  }

  /**
   * Helper method to load XML document from URL.
   * 
   * @param url source URL,
   * @param useNamespace flag to control name space awareness.
   * @return newly created XML document.
   */
  private Document loadXML(URL url, boolean useNamespace)
  {
    InputStream stream = null;
    try {
      stream = url.openConnection().getInputStream();
      InputSource source = new InputSource(stream);
      return useNamespace ? loadXMLNS(source) : loadXML(source);
    }
    catch(Exception e) {
      throw new DomException(e);
    }
    finally {
      close(stream);
    }
  }

  // ----------------------------------------------------
  // create empty HTML document

  @Override
  public Document createHTML()
  {
    return new DocumentImpl(new HTMLDocumentImpl());
  }

  // ----------------------------------------------------
  // load HTML document from string source

  @Override
  public Document parseHTML(String string)
  {
    notNullOrEmpty(string, "Source string");
    return loadHTML(new ByteArrayInputStream(string.getBytes()));
  }

  @Override
  public Document parseHTMLNS(String string)
  {
    notNullOrEmpty(string, "Source string");
    return loadHTMLNS(new ByteArrayInputStream(string.getBytes()));
  }

  // ----------------------------------------------------
  // load HTML document from file

  @Override
  public Document loadHTML(File file) throws FileNotFoundException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadHTML(new FileInputStream(file));
  }

  @Override
  public Document loadHTMLNS(File file) throws FileNotFoundException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadHTMLNS(new FileInputStream(file));
  }

  @Override
  public Document loadHTML(File file, String encoding) throws FileNotFoundException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTML(new FileInputStream(file), encoding);
  }

  @Override
  public Document loadHTMLNS(File file, String encoding) throws FileNotFoundException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTMLNS(new FileInputStream(file), encoding);
  }

  // ----------------------------------------------------
  // load HTML document from input stream

  @Override
  public Document loadHTML(InputStream stream)
  {
    notNull(stream, "Source stream");
    return loadHTML(new InputSource(stream));
  }

  @Override
  public Document loadHTMLNS(InputStream stream)
  {
    notNull(stream, "Source stream");
    return loadHTMLNS(new InputSource(stream));
  }

  @Override
  public Document loadHTML(InputStream stream, String encoding)
  {
    notNull(stream, "Source stream");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTML(new InputSource(stream), encoding);
  }

  @Override
  public Document loadHTMLNS(InputStream stream, String encoding)
  {
    notNull(stream, "Source stream");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTMLNS(new InputSource(stream), encoding);
  }

  // ----------------------------------------------------
  // load HTML document from reader

  @Override
  public Document loadHTML(Reader reader)
  {
    notNull(reader, "Source reader");
    return loadHTML(reader, Charset.defaultCharset().name());
  }

  @Override
  public Document loadHTMLNS(Reader reader)
  {
    notNull(reader, "Source reader");
    return loadHTMLNS(reader, Charset.defaultCharset().name());
  }

  @Override
  public Document loadHTML(Reader reader, String encoding)
  {
    notNull(reader, "Source reader");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTML(new InputSource(reader), encoding);
  }

  @Override
  public Document loadHTMLNS(Reader reader, String encoding)
  {
    notNull(reader, "Source reader");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTMLNS(new InputSource(reader), encoding);
  }

  // ----------------------------------------------------
  // load HTML document from input source

  @Override
  public Document loadHTML(InputSource source)
  {
    notNull(source, "Source");
    return loadHTML(source, "UTF-8");
  }

  @Override
  public Document loadHTMLNS(InputSource source)
  {
    notNull(source, "Source");
    return loadHTMLNS(source, "UTF-8");
  }

  @Override
  public Document loadHTML(InputSource source, String encoding)
  {
    notNull(source, "Source");
    notNullOrEmpty(encoding, "Characters encoding");
    source.setEncoding(encoding);
    try {
      return loadHTML(source, false);
    }
    catch(Exception e) {
      throw new DomException(e);
    }
    finally {
      close(source);
    }
  }

  @Override
  public Document loadHTMLNS(InputSource source, String encoding)
  {
    notNull(source, "Source");
    notNullOrEmpty(encoding, "Characters encoding");
    source.setEncoding(encoding);
    try {
      return loadHTML(source, true);
    }
    catch(Exception e) {
      throw new DomException(e);
    }
    finally {
      close(source);
    }
  }

  /**
   * Helper for loading HTML document from input source.
   * 
   * @param source input source,
   * @param useNamespace flag set to true if document should be name space aware.
   * @return newly created HTML document.
   * @throws SAXException if input source is not valid XML,
   * @throws IOException if reading from input stream fails.
   */
  private static Document loadHTML(InputSource source, boolean useNamespace) throws SAXException, IOException
  {
    notNull(source, "Source");
    DOMParser parser = new DOMParser();
    // source http://nekohtml.sourceforge.net/faq.html#hierarchy
    parser.setFeature(FEAT_NAMESPACES, useNamespace);
    parser.parse(source);
    return new DocumentImpl(parser.getDocument());
  }

  // ----------------------------------------------------
  // load HTML document from URL

  @Override
  public Document loadHTML(URL url)
  {
    notNull(url, "Source URL");
    return loadHTML(url, "UTF-8", false);
  }

  @Override
  public Document loadHTML(URL url, String encoding) throws IllegalArgumentException
  {
    notNull(url, "Source URL");
    notNullOrEmpty(encoding, "Character encoding");
    return loadHTML(url, encoding, false);
  }

  @Override
  public Document loadHTMLNS(URL url)
  {
    notNull(url, "Source URL");
    return loadHTML(url, "UTF-8", true);
  }

  @Override
  public Document loadHTMLNS(URL url, String encoding) throws IllegalArgumentException
  {
    notNull(url, "Source URL");
    notNullOrEmpty(encoding, "Character encoding");
    return loadHTML(url, encoding, true);
  }

  /**
   * Helper method for HTML loading from URL.
   * 
   * @param url HTML document hyper source,
   * @param useNamespace flag true if loaded document instance should have name space support.
   * @return newly created and loaded document instance.
   */
  private static Document loadHTML(URL url, String encoding, boolean useNamespace)
  {
    InputStream stream = null;
    try {
      stream = url.openConnection().getInputStream();
      InputSource source = new InputSource(stream);
      source.setEncoding(encoding);
      return loadHTML(source, useNamespace);
    }
    catch(Exception e) {
      throw new DomException(e);
    }
    finally {
      close(stream);
    }
  }

  // ----------------------------------------------------

  /**
   * Close input source.
   * 
   * @param source input source to be closed.
   */
  private static void close(InputSource source)
  {
    if(source != null) {
      if(source.getByteStream() != null) {
        close(source.getByteStream());
      }
      if(source.getCharacterStream() != null) {
        close(source.getCharacterStream());
      }
    }
  }

  /**
   * Close closeable converting IO exception to unchecked DOM exception.
   * 
   * @param closeable closeable to close.
   */
  private static void close(Closeable closeable)
  {
    try {
      if(closeable != null) {
        closeable.close();
      }
    }
    catch(IOException e) {
      throw new DomException(e);
    }
  }

  /**
   * Document building error handler.
   * 
   * @author Iulian Rotaru
   */
  static class ErrorHandlerImpl implements ErrorHandler
  {
    /**
     * Record parser fatal error to builder class logger.
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
      log.fatal(exception);
    }

    /**
     * Record parser error to builder class logger.
     */
    public void error(SAXParseException exception) throws SAXException
    {
      log.error(exception);
    }

    /**
     * Record parser warning to builder class logger.
     */
    public void warning(SAXParseException exception) throws SAXException
    {
      log.warn(exception);
    }
  }

  /**
   * Get XML document builder.
   * 
   * @param schema XML schema,
   * @param useNamespace flag to use name space.
   * @return XML document builder.
   * @throws ParserConfigurationException if document builder factory feature set fail.
   */
  private static javax.xml.parsers.DocumentBuilder getDocumentBuilder(Schema schema, boolean useNamespace) throws ParserConfigurationException
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setIgnoringComments(true);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setCoalescing(true);

    if(schema != null) {
      // because schema is used throws fatal error if XML document contains DOCTYPE declaration
      dbf.setFeature(FEAT_DOCTYPE_DECL, true);

      // excerpt from document builder factory api:
      // Note that "the validation" here means a validating parser as defined in the XML recommendation. In other words,
      // it essentially just controls the DTD validation.
      // To use modern schema languages such as W3C XML Schema or RELAX NG instead of DTD, you can configure your parser
      // to be a non-validating parser by leaving the setValidating(boolean) method false, then use the
      // setSchema(Schema)
      // method to associate a schema to a parser.
      dbf.setValidating(false);

      // XML schema validation requires namespace support
      dbf.setFeature(FEAT_SCHEMA_VALIDATION, true);
      dbf.setNamespaceAware(true);
      dbf.setSchema(schema);
    }
    else {
      // disable parser XML schema support; it is enabled by default
      dbf.setFeature(FEAT_SCHEMA_VALIDATION, false);
      dbf.setValidating(false);
      dbf.setNamespaceAware(useNamespace);
    }

    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
    db.setEntityResolver(new EntityResolverImpl());
    db.setErrorHandler(new ErrorHandlerImpl());
    return db;
  }
}
