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

@SuppressWarnings("all") public class all_keys_$Expanded_0_0 extends Strategy 
{ 
  public static all_keys_$Expanded_0_0 instance = new all_keys_$Expanded_0_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    context.push("all_keys_Expanded_0_0");
    Fail1389:
    { 
      IStrategoTerm a_32797 = null;
      a_32797 = term;
      term = dr_all_keys_0_1.instance.invoke(context, a_32797, generated.const217);
      if(term == null)
        break Fail1389;
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}