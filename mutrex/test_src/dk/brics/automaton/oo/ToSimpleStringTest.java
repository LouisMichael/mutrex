package dk.brics.automaton.oo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dk.brics.automaton.ExtendedRegex;
import dk.brics.automaton.OORegexConverter;
import dk.brics.automaton.RegExp;

public class ToSimpleStringTest {

	@Test
	public void testConvertToRegexStringSomeExamplesOmo() {
		// these must be converted back to exactly the same string
		checkOmoTrasformation("[a-b]");		
		checkOmoTrasformation("a+");		
		checkOmoTrasformation("a*");		
		checkOmoTrasformation("ab*");		
		checkOmoTrasformation("(ab)*");		
	}

	@Test
	public void testConvertToRegexStringSomeExamplesEquivalent() {
		assertEquals("(a|b)*", convertBack("[ab]*"));
	}
	
	
	private void checkOmoTrasformation(String s){
		// convert
		assertEquals(s, convertBack(s));
	}


	private String convertBack(String s) {
		RegExp regex = ExtendedRegex.getSimplifiedRegexp(s);		
		String result = ToSimpleString.convertToReadableString(OORegexConverter.getOORegex(regex));
		return result;
	}
	
}
