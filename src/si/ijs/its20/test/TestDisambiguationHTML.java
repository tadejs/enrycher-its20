package si.ijs.its20.test;

import org.junit.Test;

public class TestDisambiguationHTML {

	protected static final String disambigInputRoot = "ITS-2.0-Testsuite/its2.0/inputdata/textanalysis";
	protected static final String disambigOutputRoot = "ITS-2.0-Testsuite/its2.0/expected/textanalysis";
	protected static final String disambigMyOutputRoot = "output/textanalysis";
	
	protected void testHTMLDisambig(String testName) {
		//System.out.println("====" +testName);
		TestUtils.runTestHTML(disambigInputRoot, disambigOutputRoot, disambigMyOutputRoot, testName);
	}
	
	
	@Test
	public void testHTML1() {
		testHTMLDisambig("textanalysis1html");
	}

	@Test
	public void testHTML2() {
		testHTMLDisambig("textanalysis2html");
	}

	
	@Test
	public void testHTML3() {
		testHTMLDisambig("textanalysis3html");
	}

	
	@Test
	public void testHTML4() {
		testHTMLDisambig("textanalysis4html");
	}
	

	@Test
	public void testHTML5() {
		testHTMLDisambig("textanalysis5html");
	}

}
