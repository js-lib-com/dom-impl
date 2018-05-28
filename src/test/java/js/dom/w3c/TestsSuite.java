package js.dom.w3c;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestsSuite extends TestCase {
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(BuilderUnitTest.class);
		suite.addTestSuite(DocumentUnitTest.class);
		suite.addTestSuite(ElementUnitTest.class);
		suite.addTestSuite(EListUnitTest.class);
		suite.addTestSuite(SerializerUnitTest.class);
		suite.addTestSuite(XPathUnitTest.class);
		return suite;
	}
}
