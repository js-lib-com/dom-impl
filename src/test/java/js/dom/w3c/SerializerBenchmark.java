package js.dom.w3c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import js.dom.Document;
import js.util.Classes;
import junit.framework.TestCase;

public class SerializerBenchmark extends TestCase {
	private static final int TEST_COUNT = 10000;

	private Document doc;
	private Writer writer;
	org.w3c.dom.Document document;

	@Override
	protected void setUp() throws Exception {
		this.doc = new DocumentBuilderImpl().loadHTML(new File("fixture/bench-probe.html"));
		this.document = Classes.getFieldValue(this.doc, "doc");
		this.writer = new MockWriter();
	}

	public void testBbDomSerialize() throws IOException {
		Metter metter = new Metter("Baby DOM serialize");
		for (int i = 0; i < TEST_COUNT; ++i) {
			this.doc.serialize(this.writer);
		}
		metter.stop();
	}

	public void testApacheOutputFormat() throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(this.writer);
		Metter metter = new Metter("Apache OutputFormat");
		for (int i = 0; i < TEST_COUNT; ++i) {
			DOMImplementationLS DOM = null;
			try {
				DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
				DOM = (DOMImplementationLS) registry.getDOMImplementation("XML 3.0 LS 3.0");
			} catch (Exception e) {
				// do not use logger to record error; at this point logger may be not initialized yet
				System.out.println("Bad server libraries. Missing DOMImplementation.");
				return;
			}

			LSSerializer serializer = DOM.createLSSerializer();
			LSOutput output = DOM.createLSOutput();
			output.setEncoding("UTF-8");
			output.setCharacterStream(bufferedWriter);
			serializer.write(document, output);

		}
		metter.stop();
	}

	public void testJavaxTransformer() throws TransformerFactoryConfigurationError, TransformerException {
		BufferedWriter bufferedWriter = new BufferedWriter(this.writer);
		Metter metter = new Metter("Javax Transformer");
		for (int i = 0; i < TEST_COUNT; ++i) {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.0 Transitional//EN");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			Source source = new DOMSource(this.document);
			Result result = new StreamResult(bufferedWriter);
			transformer.transform(source, result);
		}
		metter.stop();
	}

	private static class Metter {
		private long start;

		public Metter(String label) {
			System.out.print(TEST_COUNT + " " + label + ": ");
			this.start = new Date().getTime();
		}

		public void stop() {
			System.out.println(new Date().getTime() - this.start);
		}
	}

	private static class MockWriter extends Writer {
		@Override
		public void close() throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
		}
	}
}
