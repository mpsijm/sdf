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

@SuppressWarnings("all") public class $Block$Comment$Defs_1_0 extends Strategy 
{ 
  public static $Block$Comment$Defs_1_0 instance = new $Block$Comment$Defs_1_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy j_32791)
  { 
    ITermFactory termFactory = context.getFactory();
    context.push("BlockCommentDefs_1_0");
    Fail1287:
    { 
      IStrategoTerm d_32968 = null;
      IStrategoTerm c_32968 = null;
      if(term.getTermType() != IStrategoTerm.APPL || Main._consBlockCommentDefs_1 != ((IStrategoAppl)term).getConstructor())
        break Fail1287;
      c_32968 = term.getSubterm(0);
      IStrategoList annos58 = term.getAnnotations();
      d_32968 = annos58;
      term = j_32791.invoke(context, c_32968);
      if(term == null)
        break Fail1287;
      term = termFactory.annotateTerm(termFactory.makeAppl(Main._consBlockCommentDefs_1, new IStrategoTerm[]{term}), checkListAnnos(termFactory, d_32968));
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}