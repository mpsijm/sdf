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

@SuppressWarnings("all") final class lifted305 extends Strategy 
{ 
  public static final lifted305 instance = new lifted305();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail1711:
    { 
      IStrategoTerm term386 = term;
      Success626:
      { 
        Fail1712:
        { 
          term = string_is_layout_0_0.instance.invoke(context, term);
          if(term == null)
            break Fail1712;
          { 
            if(true)
              break Fail1711;
            if(true)
              break Success626;
          }
        }
        term = term386;
      }
      if(true)
        return term;
    }
    return null;
  }
}