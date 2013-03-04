package si.ijs.its20.test;

import org.junit.Test;

public class TestDisambiguationXML {

	protected static final String disambigInputRoot = "ITS-2.0-Testsuite/its2.0/inputdata/textanalysis";
	protected static final String disambigMyOutputRoot = "output/textanalysis";
	protected static final String disambigOutputRoot = "ITS-2.0-Testsuite/its2.0/expected/textanalysis";
	
	protected void testXMLDisambig(String testName) {
		//System.out.println("====" +testName);
		TestUtils.runTestXML(disambigInputRoot, disambigOutputRoot, disambigMyOutputRoot, testName);
	}
	
	@Test
	public void test1XML() {
		testXMLDisambig("textanalysis1xml");
	}
	
	@Test
	public void test2XML() {
		testXMLDisambig("textanalysis2xml");
	}
	
	@Test
	public void test3XML() {
		testXMLDisambig("textanalysis3xml");
	}
	

	@Test
	public void test4XML() {
		testXMLDisambig("textanalysis4xml");
	}
	

	@Test
	public void test5XML() {
		testXMLDisambig("textanalysis5xml");
	}
	
	@Test
	public void test6XML() {
		testXMLDisambig("textanalysis6xml");
	}
	

	@Test
	public void test7XML() {
		testXMLDisambig("textanalysis7xml");
	}
	
	@Test
	public void test8XML() {
		testXMLDisambig("textanalysis8xml");
	}
}
