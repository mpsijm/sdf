package org.strategoxt.imp.editors.template.generated;

import org.strategoxt.stratego_lib.*;
import org.strategoxt.stratego_lib.*;
import org.strategoxt.stratego_sglr.*;
import org.strategoxt.stratego_gpp.*;
import org.strategoxt.stratego_xtc.*;
import org.strategoxt.stratego_aterm.*;
import org.strategoxt.stratego_sdf.*;
import org.strategoxt.strc.*;
import org.strategoxt.imp.editors.template.generated.*;
import org.strategoxt.java_front.*;
import org.strategoxt.lang.*;
import org.spoofax.interpreter.terms.*;
import static org.strategoxt.lang.Term.*;
import org.spoofax.interpreter.library.AbstractPrimitive;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

@SuppressWarnings("all") public class $Table$Provider_1_0 extends Strategy 
{ 
  public static $Table$Provider_1_0 instance = new $Table$Provider_1_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy q_32791)
  { 
    ITermFactory termFactory = context.getFactory();
    context.push("TableProvider_1_0");
    Fail1294:
    { 
      IStrategoTerm y_32968 = null;
      IStrategoTerm x_32968 = null;
      if(term.getTermType() != IStrategoTerm.APPL || Main._consTableProvider_1 != ((IStrategoAppl)term).getConstructor())
        break Fail1294;
      x_32968 = term.getSubterm(0);
      IStrategoList annos65 = term.getAnnotations();
      y_32968 = annos65;
      term = q_32791.invoke(context, x_32968);
      if(term == null)
        break Fail1294;
      term = termFactory.annotateTerm(termFactory.makeAppl(Main._consTableProvider_1, new IStrategoTerm[]{term}), checkListAnnos(termFactory, y_32968));
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}