package org.metaborg.newsdf2table.grammar;

import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SequenceSymbol extends Symbol {

    Symbol first;
    List<Symbol> tail;

    public SequenceSymbol(Symbol first, List<Symbol> tail) {        
        this.first = first;
        this.tail = tail;
        followRestrictions = Sets.newHashSet();
    }

    @Override public String name() {
        String name = first.name() + " ";
        for(Symbol s : tail) {
            name += s.name() + " ";
        }
        return name;
    }

    @Override public String toString() {
        return name();
    }

    @Override public IStrategoTerm toAterm(ITermFactory tf) {
        List<IStrategoTerm> tail_aterm = Lists.newArrayList();

        for(Symbol s : tail) {
            tail_aterm.add(s.toAterm(tf));
        }

        return tf.makeAppl(tf.makeConstructor("seq", 2), first.toAterm(tf), tf.makeList(tail_aterm));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((tail == null) ? 0 : tail.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!super.equals(obj))
            return false;
        if(getClass() != obj.getClass())
            return false;
        SequenceSymbol other = (SequenceSymbol) obj;
        if(first == null) {
            if(other.first != null)
                return false;
        } else if(!first.equals(other.first))
            return false;
        if(tail == null) {
            if(other.tail != null)
                return false;
        } else if(!tail.equals(other.tail))
            return false;
        return true;
    }
}