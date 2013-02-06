package si.ijs.its20.test;

import org.junit.Test;

public class TestDisambiguationHTML {

	protected static final String disambigInputRoot = "ITS-2.0-Testsuite/its2.0/inputdata/disambiguation";
	protected static final String disambigOutputRoot = "ITS-2.0-Testsuite/its2.0/expected/disambiguation";
	protected static final String disambigMyOutputRoot = "output/disambiguation";
	
	protected void testHTMLDisambig(String testName) {
		//System.out.println("====" +testName);
		TestUtils.runTestHTML(disambigInputRoot, disambigOutputRoot, disambigMyOutputRoot, testName);
	}
	
	
	@Test
	public void testHTML1() {
		testHTMLDisambig("disambiguation1html");
	}

	@Test
	public void testHTML2() {
		testHTMLDisambig("disambiguation2html");
	}

	
	@Test
	public void testHTML3() {
		testHTMLDisambig("disambiguation3html");
	}

	
	@Test
	public void testHTML4() {
		testHTMLDisambig("disambiguation4html");
	}
	

	@Test
	public void testHTML5() {
		testHTMLDisambig("disambiguation5html");
	}

}
