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

@SuppressWarnings("all") public class templatelang_option_desugar_0_0 extends Strategy 
{ 
  public static templatelang_option_desugar_0_0 instance = new templatelang_option_desugar_0_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    ITermFactory termFactory = context.getFactory();
    context.push("templatelang_option_desugar_0_0");
    Fail1094:
    { 
      IStrategoTerm term531 = term;
      IStrategoConstructor cons34 = term.getTermType() == IStrategoTerm.APPL ? ((IStrategoAppl)term).getConstructor() : null;
      Success456:
      { 
        if(cons34 == Main._consSeparator_1)
        { 
          Fail1095:
          { 
            IStrategoTerm j_32777 = null;
            j_32777 = term.getSubterm(0);
            term = templatelang_origin_track_forced_1_0.instance.invoke(context, j_32777, lifted408.instance);
            if(term == null)
              break Fail1095;
            term = termFactory.makeAppl(Main._consSeparator_1, new IStrategoTerm[]{term});
            if(true)
              break Success456;
          }
          term = term531;
        }
        if(cons34 == Main._consText_1)
        { 
          IStrategoTerm g_32777 = null;
          g_32777 = term.getSubterm(0);
          term = templatelang_origin_track_forced_1_0.instance.invoke(context, g_32777, lifted409.instance);
          if(term == null)
            break Fail1094;
          term = termFactory.makeAppl(Main._consText_1, new IStrategoTerm[]{term});
        }
        else
        { 
          break Fail1094;
        }
      }
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}