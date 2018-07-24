package regex.operators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dk.brics.automaton.oo.REGEXP_CHAR;
import dk.brics.automaton.oo.REGEXP_CHAR_RANGE;
import dk.brics.automaton.oo.REGEXP_CONCATENATION;
import dk.brics.automaton.oo.REGEXP_REPEAT;
import dk.brics.automaton.oo.REGEXP_SPECIALCHAR;
import dk.brics.automaton.oo.ooregex;
import dk.brics.automaton.oo.oosimpleexp;

/**
 * substitutes a char (. # @) with the correspondent metachar. The fault is the
 * the user has wrongly used a char instead of a metachar.
 *
 * In MUTATION 2017 is C2M
 * 
 */
public class Char2MetaChar extends RegexMutator {
	public static Char2MetaChar mutator = new Char2MetaChar();

	private Char2MetaChar() {
		super(new Char2MetaCharVisitor());
	}

	private static class Char2MetaCharVisitor extends RegexVisitorAdapterList {
		@Override
		public List<ooregex> visit(REGEXP_CHAR r) {
			// if equal to some metachar
			if (r.isMetaChar()) {
				// System.out.println(r);
				return Collections.singletonList((ooregex) new REGEXP_SPECIALCHAR(r.c));
			} else {
				// not a meta char
				return Collections.EMPTY_LIST;
			}
		}

		@Override
		public List<ooregex> visit(oosimpleexp r) {
			List<ooregex> result = new ArrayList<>();
			// chek when "a-b" as string 
			int minusI = r.s.indexOf('-');
			if (minusI > 0 && minusI < r.s.length() - 1) {
				// split the string
				String p0 = r.s.substring(0, minusI - 1);
				// TODO recursive over p0
				char p1 = r.s.charAt(minusI - 1);
				char p2 = r.s.charAt(minusI + 1);
				String p3 = r.s.substring(minusI + 2);
				// TODO recursive over p3
				// System.out.println(r.s + " ->" + p0 + "#" + p1 + "#" + p2 +
				// "#" + p3);
				if (p2 > p1) {
					ooregex range = new REGEXP_CHAR_RANGE(p1, p2);
					if (p0.length() > 0)
						range = new REGEXP_CONCATENATION(oosimpleexp.createoosimpleexp(p0), range);
					if (p3.length() > 0)
						range = new REGEXP_CONCATENATION(range, oosimpleexp.createoosimpleexp(p3));
					return Collections.singletonList(range);
				}
				return Collections.EMPTY_LIST;
			}
			result.addAll(checkMetaChar(r, "+"));
			result.addAll(checkMetaChar(r, "*"));
			result.addAll(checkMetaChar(r, "?"));
			/* .	(any single character)	
				|	#	(the empty language)	[OPTIONAL]
				|	@	(any string)	[OPTIONAL]
			*/
			result.addAll(checkMetaChar(r, "."));
			result.addAll(checkMetaChar(r, "#"));
			result.addAll(checkMetaChar(r, "@"));
			return result;
		}

		private List<ooregex> checkMetaChar(oosimpleexp r, String rsymb) {
			assert rsymb.length() == 1;
			if (r.s.contains(rsymb)) {
				//System.err.println(r + " " + rsymb);
				List<ooregex> result = new ArrayList<>();
				// quantifier in the string
				// split the string
				String prefix = r.s.substring(0, r.s.indexOf(rsymb));
				String postfix = r.s.substring(r.s.indexOf(rsymb) + 1, r.s.length());
				// visit the second half - in case there are more
				if(postfix.length() > 0) {
					ooregex rest = oosimpleexp.createoosimpleexp(postfix);
					List<ooregex> resultRest = rest.accept(this);
					for (ooregex mr : resultRest) {
						// re add the symbol
						result.add(new REGEXP_CONCATENATION(oosimpleexp.createoosimpleexp(prefix + rsymb), mr));
					}
				}
				if(prefix.length() > 0) {
					ooregex prefixOOr = oosimpleexp.createoosimpleexp(prefix);
					// 
					ooregex rp = null;
					switch (rsymb) {
					case "+":
						rp = REGEXP_REPEAT.REGEXP_REPEAT_MIN(prefixOOr);
						break;
					case "*":
						rp = REGEXP_REPEAT.REGEXP_REPEAT(prefixOOr);
						break;
					case "?":
						rp = REGEXP_REPEAT.REGEXP_OPTIONAL(prefixOOr);
						break;
					case ".":
					case "#":
					case "@":
						rp = new REGEXP_CONCATENATION(prefixOOr,new REGEXP_SPECIALCHAR(rsymb.charAt(0)));
						break;
					default:
						assert false : " Char" + rsymb;
					}
					if (postfix.length()>0)
						result.add(new REGEXP_CONCATENATION(rp, oosimpleexp.createoosimpleexp(postfix)));
					else
						result.add(rp);
				}
				// System.out.println(result);
				return result;
			} else {
				return Collections.EMPTY_LIST;
			}
		}

		@Override
		public String getCode() {
			return "C2M";
		}
	}

	@Override
	public String getCode() {
		return "C2M";
	}
}
