package si.ijs.its20.test;


import org.junit.Test;

public class TestDomainHTML {
		
	protected void testHTMLDomain(String testName) {
		//System.out.println("====" +testName);
		TestUtils.runTestHTML( "ITS-2.0-Testsuite/its2.0/inputdata/domain", "ITS-2.0-Testsuite/its2.0/expected/domain", "output/domain", testName);
	}
	
	
	@Test
	public void testDomain1() {
		testHTMLDomain("domain1html");
	}
	@Test
	public void testDomain2() {
		testHTMLDomain("domain2html");
	}
	@Test
	public void testDomain3() {
		testHTMLDomain("domain3html");
	}
	
	@Test
	public void testDomain4() {
		testHTMLDomain("domain4html");
	}
}
