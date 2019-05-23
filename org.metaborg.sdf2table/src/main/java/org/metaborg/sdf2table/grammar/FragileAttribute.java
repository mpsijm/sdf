package org.metaborg.sdf2table.grammar;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class FragileAttribute implements IAttribute {
    @Override public IStrategoTerm toAterm(ITermFactory tf) {
        return tf.makeAppl(tf.makeConstructor("fragile", 0));
    }

    @Override public IStrategoTerm toSDF3Aterm(ITermFactory tf) {
        return null;
    }
}
