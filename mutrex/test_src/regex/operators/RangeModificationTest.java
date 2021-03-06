package regex.operators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static regex.operators.RangeModification.mutator;

import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.OORegexConverter;
import dk.brics.automaton.RegExp;
import regex.operators.RegexMutator.MutatedRegExp;
import regex.utils.IteratorUtils;

public class RangeModificationTest extends RegexMutationTest {

	@BeforeClass
	static public void setup() {
		ConsoleHandler ch = new ConsoleHandler();
		Logger.getLogger(OORegexConverter.class.getName()).addHandler(ch);
		Logger.getLogger(OORegexConverter.class.getName()).setLevel(Level.ALL);
	}

	@Test
	public void testMutate() {
		RegExp re = new RegExp("[c-e]");
		accept(re, "c", "d", "e");
		acceptNot(re, "b", "f");
		acceptNot(re, "a", "g");
		Iterator<MutatedRegExp> res = mutator.mutate(re);//
		int acceptB = 0;
		int acceptF = 0;
		for (int i = 0; i < 4; i++) {
			RegExp corrected = res.next().mutatedRexExp;
			// each will accept d
			accept(corrected, "d");
			// no one will accept these
			acceptNot(re, "a", "g");
			// one will accept b and another will accept f
			Automaton ca = corrected.toAutomaton();
			if (ca.run("b"))
				acceptB++;
			if (ca.run("f"))
				acceptF++;
		}
		assertFalse(res.hasNext());
		assertEquals(1, acceptB);
		assertEquals(1, acceptF);
	}

	@Test
	public void testDoNoMutate() {
		RegExp re = new RegExp("[;-=]");
		Iterator<MutatedRegExp> res = mutator.mutate(re);
		assertFalse(res.hasNext());
	}

	@Test
	public void testExamplePaperSI_mutation2017() {
		RegExp re = new RegExp("[f-m]");
		List<MutatedRegExp> mutants = IteratorUtils.iteratorToList(mutator.mutate(re));
		for (MutatedRegExp m : mutants) {
			System.out.println(m);
		}
		assertEquals(4, mutants.size());
	}
}