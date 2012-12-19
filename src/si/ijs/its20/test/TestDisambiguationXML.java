package si.ijs.its20.test;

import org.junit.Test;

public class TestDisambiguationXML {

	protected static final String disambigInputRoot = "ITS-2.0-Testsuite/its2.0/inputdata/disambiguation";
	protected static final String disambigMyOutputRoot = "output/disambiguation";
	protected static final String disambigOutputRoot = "ITS-2.0-Testsuite/its2.0/expected/disambiguation";
	
	protected void testXMLDisambig(String testName) {
		System.out.println("====" +testName);
		TestUtils.runTestXML(disambigInputRoot, disambigOutputRoot, disambigMyOutputRoot, testName);
	}
	
	@Test
	public void test1XML() {
		testXMLDisambig("disambiguation1xml");
	}
	
	@Test
	public void test2XML() {
		testXMLDisambig("disambiguation2xml");
	}
	
	@Test
	public void test3XML() {
		testXMLDisambig("disambiguation3xml");
	}
	

	@Test
	public void test4XML() {
		testXMLDisambig("disambiguation4xml");
	}
	

	@Test
	public void test5XML() {
		testXMLDisambig("disambiguation5xml");
	}
	
	@Test
	public void test6XML() {
		testXMLDisambig("disambiguation6xml");
	}
	

	@Test
	public void test7XML() {
		testXMLDisambig("disambiguation7xml");
	}
	
	@Test
	public void test8XML() {
		testXMLDisambig("disambiguation8xml");
	}
}
