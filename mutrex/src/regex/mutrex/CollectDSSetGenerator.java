package regex.mutrex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import regex.distinguishing.DistinguishingString;
import regex.operators.RegexMutator.MutatedRegExp;

/**
 * generates a ds that tries to kill as many mutants as possible
 * 
 * @author garganti
 *
 */
abstract class CollectDSSetGenerator extends DSSetGenerator {
	private static Logger logger = Logger.getLogger(CollectDSSetGenerator.class.getName());

	@Override
	public void addStringsToDSSet(DSSet result, RegExp regex, Iterator<MutatedRegExp> mutants) {
		List<Boolean> trueFalse = Arrays.asList(true, false);
		Automaton rexAut = regex.toAutomaton();
		List<DistinguishingAutomaton> das = new ArrayList<>();
		nextMut: while (mutants.hasNext()) {
			RegExp mutant = mutants.next().mutatedRexExp;
			sortDAs(das);
			Automaton mAut = null;
			Automaton negMaut = null;
			Iterator<DistinguishingAutomaton> dasIt = das.iterator();
			while (dasIt.hasNext()) {
				DistinguishingAutomaton da = dasIt.next();
				// solution 2: invalidating the da
				/*if(!da.isActive) {
					continue;
				}*/

				if (da.add(mutant, mAut, negMaut)) {
					logger.log(Level.INFO, "collected mutation " + mutant + " into " + da);
					if (stop(da)) {
						// solution 1: removing the da
						genTest(result, da);
						dasIt.remove();

						//solution 2: invalidating the da
						//da.isActive = false;
					}
					continue nextMut;
				}
			}
			// it is not collectable
			// try to collect rexAut
			Collections.shuffle(trueFalse);
			for (boolean b : trueFalse) {
				DistinguishingAutomaton newDa = new DistinguishingAutomaton(rexAut, b);
				if (newDa.add(mutant, mAut, negMaut)) {
					das.add(newDa);
					logger.log(Level.INFO, "new da for mutation " + mutant + " into " + newDa);
					continue nextMut;
				}
			}
			// it is equivalent
			logger.log(Level.INFO, "equivalent mutation " + mutant);
		}
		// now get the remaining DS
		for (DistinguishingAutomaton da : das) {
			genTest(result, da);
		}
		return;
	}

	private void sortDAs(List<DistinguishingAutomaton> das) {
		Collections.shuffle(das);
		//smallest ones first
		//das.sort((DistinguishingAutomaton o1, DistinguishingAutomaton o2) -> o1.mutatedRegexes.size() - o2.mutatedRegexes.size());
		//biggest ones first
		//das.sort((DistinguishingAutomaton o1, DistinguishingAutomaton o2) -> o2.mutatedRegexes.size() - o1.mutatedRegexes.size());
	}

	private void genTest(DSSet result, DistinguishingAutomaton da) {
		DistinguishingString ds = new DistinguishingString(da.getExample(), da.isPositive());
		result.add(ds, da.getMutants());
	}

	abstract boolean stop(DistinguishingAutomaton da);
}