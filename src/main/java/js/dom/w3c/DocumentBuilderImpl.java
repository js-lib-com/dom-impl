package js.dom.w3c;

import static js.util.Params.isFalse;
import static js.util.Params.notNull;
import static js.util.Params.notNullOrEmpty;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
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
   */
  private static Document createXML(String root, boolean useNamespace)
  {
    notNullOrEmpty(root, "Root element");
    org.w3c.dom.Document doc = getDocumentBuilder(null, useNamespace).newDocument();
    doc.appendChild(doc.createElement(root));
    return new DocumentImpl(doc);
  }

  // ----------------------------------------------------
  // parse XML document from string source

  @Override
  public Document parseXML(String string) throws SAXException
  {
    notNullOrEmpty(string, "Source string");
    try {
      return loadXML(new ByteArrayInputStream(string.getBytes("UTF-8")));
    }
    catch(UnsupportedEncodingException e) {
      throw new BugError("JVM with missing support for UTF-8.");
    }
    catch(IOException e) {
      throw new BugError("IO exception while reading string.");
    }
  }

  @Override
  public Document parseXMLNS(String string) throws SAXException
  {
    notNullOrEmpty(string, "Source string");
    try {
      return loadXMLNS(new ByteArrayInputStream(string.getBytes("UTF-8")));
    }
    catch(UnsupportedEncodingException e) {
      throw new BugError("JVM with missing support for UTF-8.");
    }
    catch(IOException e) {
      throw new BugError("IO exception while reading string.");
    }
  }

  // ----------------------------------------------------
  // load XML document from file

  @Override
  public Document loadXML(File file) throws IOException, SAXException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadXML(new FileInputStream(file));
  }

  @Override
  public Document loadXMLNS(File file) throws IOException, SAXException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadXMLNS(new FileInputStream(file));
  }

  // ----------------------------------------------------
  // load XML document from input stream

  @Override
  public Document loadXML(InputStream stream) throws IOException, SAXException
  {
    notNull(stream, "Input stream");
    return loadXML(new InputSource(stream), false);
  }

  @Override
  public Document loadXMLNS(InputStream stream) throws IOException, SAXException
  {
    notNull(stream, "Input stream");
    return loadXML(new InputSource(stream), true);
  }

  // ----------------------------------------------------
  // load XML document from reader

  @Override
  public Document loadXML(Reader reader) throws IOException, SAXException
  {
    notNull(reader, "Source reader");
    return loadXML(new InputSource(reader), false);
  }

  @Override
  public Document loadXMLNS(Reader reader) throws IOException, SAXException
  {
    notNull(reader, "Source reader");
    return loadXML(new InputSource(reader), true);
  }

  // ----------------------------------------------------
  // load XML document from input source

  /**
   * Helper method to load XML document from input source.
   * 
   * @param source input source,
   * @param useNamespace flag to control name space awareness.
   * @return newly created XML document.
   * @throws IOException input source reading fails.
   * @throws SAXException input source content is not a valid XML document.
   */
  private static Document loadXML(InputSource source, boolean useNamespace) throws IOException, SAXException
  {
    try {
      org.w3c.dom.Document doc = getDocumentBuilder(null, useNamespace).parse(source);
      return new DocumentImpl(doc);
    }
    finally {
      close(source);
    }
  }

  // ----------------------------------------------------
  // load XML document from URL

  @Override
  public Document loadXML(URL url) throws IOException, SAXException
  {
    notNull(url, "Source URL");
    return loadXML(url, false);
  }

  @Override
  public Document loadXMLNS(URL url) throws IOException, SAXException
  {
    notNull(url, "Source URL");
    return loadXML(url, true);
  }

  /**
   * Helper method to load XML document from URL.
   * 
   * @param url source document URL,
   * @param useNamespace flag to control name space awareness.
   * @return newly created XML document.
   * @throws IOException if source document reading fails.
   * @throws SAXException if source document is not valid XML.
   */
  private Document loadXML(URL url, boolean useNamespace) throws IOException, SAXException
  {
    InputStream stream = null;
    try {
      stream = url.openConnection().getInputStream();
      InputSource source = new InputSource(stream);
      return loadXML(source, useNamespace);
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
  public Document parseHTML(String string) throws SAXException
  {
    notNullOrEmpty(string, "Source string");
    try {
      return loadHTML(new ByteArrayInputStream(string.getBytes()));
    }
    catch(IOException e) {
      throw new SAXException(e.getMessage());
    }
  }

  @Override
  public Document parseHTMLNS(String string) throws SAXException
  {
    notNullOrEmpty(string, "Source string");
    try {
      return loadHTMLNS(new ByteArrayInputStream(string.getBytes()));
    }
    catch(IOException e) {
      throw new SAXException(e.getMessage());
    }
  }

  // ----------------------------------------------------
  // load HTML document from file

  @Override
  public Document loadHTML(File file) throws IOException, SAXException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadHTML(new FileInputStream(file));
  }

  @Override
  public Document loadHTMLNS(File file) throws IOException, SAXException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    return loadHTMLNS(new FileInputStream(file));
  }

  @Override
  public Document loadHTML(File file, String encoding) throws IOException, SAXException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTML(new FileInputStream(file), encoding);
  }

  @Override
  public Document loadHTMLNS(File file, String encoding) throws IOException, SAXException
  {
    notNull(file, "Source file");
    isFalse(file.isDirectory(), "Source file parameter |%s| is a directory.", file);
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTMLNS(new FileInputStream(file), encoding);
  }

  // ----------------------------------------------------
  // load HTML document from input stream

  @Override
  public Document loadHTML(InputStream stream) throws IOException, SAXException
  {
    notNull(stream, "Source stream");
    return loadHTML(new InputSource(stream), "UTF-8");
  }

  @Override
  public Document loadHTMLNS(InputStream stream) throws IOException, SAXException
  {
    notNull(stream, "Source stream");
    return loadHTMLNS(new InputSource(stream), "UTF-8");
  }

  @Override
  public Document loadHTML(InputStream stream, String encoding) throws IOException, SAXException
  {
    notNull(stream, "Source stream");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTML(new InputSource(stream), encoding);
  }

  @Override
  public Document loadHTMLNS(InputStream stream, String encoding) throws IOException, SAXException
  {
    notNull(stream, "Source stream");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTMLNS(new InputSource(stream), encoding);
  }

  // ----------------------------------------------------
  // load HTML document from reader

  @Override
  public Document loadHTML(Reader reader) throws IOException, SAXException
  {
    notNull(reader, "Source reader");
    return loadHTML(reader, Charset.defaultCharset().name());
  }

  @Override
  public Document loadHTMLNS(Reader reader) throws IOException, SAXException
  {
    notNull(reader, "Source reader");
    return loadHTMLNS(reader, Charset.defaultCharset().name());
  }

  @Override
  public Document loadHTML(Reader reader, String encoding) throws IOException, SAXException
  {
    notNull(reader, "Source reader");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTML(new InputSource(reader), encoding);
  }

  @Override
  public Document loadHTMLNS(Reader reader, String encoding) throws IOException, SAXException
  {
    notNull(reader, "Source reader");
    notNullOrEmpty(encoding, "Characters encoding");
    return loadHTMLNS(new InputSource(reader), encoding);
  }

  // ----------------------------------------------------
  // load HTML document from input source

  private static Document loadHTML(InputSource source, String encoding) throws IOException, SAXException
  {
    notNull(source, "Source");
    notNullOrEmpty(encoding, "Characters encoding");
    source.setEncoding(encoding);
    try {
      return loadHTML(source, false);
    }
    finally {
      close(source);
    }
  }

  private static Document loadHTMLNS(InputSource source, String encoding) throws IOException, SAXException
  {
    notNull(source, "Source");
    notNullOrEmpty(encoding, "Characters encoding");
    source.setEncoding(encoding);
    try {
      return loadHTML(source, true);
    }
    finally {
      close(source);
    }
  }

  /**
   * Utility method for loading HTML document from input source.
   * 
   * @param source input source,
   * @param useNamespace flag set to true if document should be name space aware.
   * @return newly created HTML document.
   * @throws IOException if reading from input stream fails.
   * @throws SAXException if input source is not valid HTML.
   */
  private static Document loadHTML(InputSource source, boolean useNamespace) throws IOException, SAXException
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
  public Document loadHTML(URL url) throws IOException, SAXException
  {
    notNull(url, "Source URL");
    return loadHTML(url, "UTF-8", false);
  }

  @Override
  public Document loadHTML(URL url, String encoding) throws IOException, SAXException
  {
    notNull(url, "Source URL");
    notNullOrEmpty(encoding, "Character encoding");
    return loadHTML(url, encoding, false);
  }

  @Override
  public Document loadHTMLNS(URL url) throws IOException, SAXException
  {
    notNull(url, "Source URL");
    return loadHTML(url, "UTF-8", true);
  }

  @Override
  public Document loadHTMLNS(URL url, String encoding) throws IOException, SAXException
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
   * @throws IOException
   * @throws SAXException
   */
  private static Document loadHTML(URL url, String encoding, boolean useNamespace) throws IOException, SAXException
  {
    InputStream stream = null;
    try {
      stream = url.openConnection().getInputStream();
      InputSource source = new InputSource(stream);
      source.setEncoding(encoding);
      return loadHTML(source, useNamespace);
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
   * @throws IOException if input source closing fails.
   */
  private static void close(InputSource source) throws IOException
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
   * @throws IOException if closing operation fails.
   */
  private static void close(Closeable closeable) throws IOException
  {
    if(closeable != null) {
      closeable.close();
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
   */
  private static javax.xml.parsers.DocumentBuilder getDocumentBuilder(Schema schema, boolean useNamespace)
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setIgnoringComments(true);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setCoalescing(true);

    try {
      if(schema != null) {
        // because schema is used throws fatal error if XML document contains DOCTYPE declaration
        dbf.setFeature(FEAT_DOCTYPE_DECL, true);

        // excerpt from document builder factory api:
        // Note that "the validation" here means a validating parser as defined in the XML recommendation. In other
        // words,
        // it essentially just controls the DTD validation.
        // To use modern schema languages such as W3C XML Schema or RELAX NG instead of DTD, you can configure your
        // parser
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
    catch(ParserConfigurationException e) {
      // document builder implementation does not support features used by this method
      throw new BugError(e);
    }
  }
}
