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

@SuppressWarnings("all") final class lifted388 extends Strategy 
{ 
  public static final lifted388 instance = new lifted388();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail1640:
    { 
      term = at_last_1_0.instance.invoke(context, term, lifted389.instance);
      if(term == null)
        break Fail1640;
      if(true)
        return term;
    }
    return null;
  }
}