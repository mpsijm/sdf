package org.metaborg.sdf2table.parsetable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.metaborg.sdf2table.core.Benchmark;
import org.metaborg.sdf2table.core.CollisionSet;
import org.metaborg.sdf2table.core.Exportable;
import org.metaborg.sdf2table.core.Utilities;
import org.metaborg.sdf2table.grammar.Production;
import org.metaborg.sdf2table.grammar.Module;
import org.metaborg.sdf2table.grammar.ModuleNotFoundException;
import org.metaborg.sdf2table.grammar.SyntaxProduction;
import org.metaborg.sdf2table.grammar.Syntax;
import org.metaborg.sdf2table.grammar.UndefinedSymbolException;
import org.metaborg.sdf2table.symbol.NonTerminal;
import org.metaborg.sdf2table.symbol.Symbol;
import org.spoofax.interpreter.terms.*;
import org.spoofax.terms.*;

public class ParseTable{ // TODO extends ParseTable from Set<State>.
	public static final int VERSION = 6;
	
	public enum PriorityPolicy{
		IGNORE,
		DEEP,
		SHALLOW
	}
	
	public enum DeepReduceConflictPolicy{
		IGNORE,
		MERGE
	}
	
	static final StrategoConstructor CONS_PT = new StrategoConstructor("parse-table", 5);
	static final StrategoConstructor CONS_STATES = new StrategoConstructor("states", 1);
	static final StrategoConstructor CONS_PRIORITIES = new StrategoConstructor("priorities", 1);
	
	static final int LOAD_FACTOR = 2;
	
	//List<State> _states = new LinkedList<>();
	CollisionSet<State> _states = null;
	List<Label> _labels = new LinkedList<>();
	
	Syntax _syntax;
	PriorityPolicy _ppolicy = PriorityPolicy.DEEP;
	DeepReduceConflictPolicy _rcpolicy = DeepReduceConflictPolicy.IGNORE;
	
	Queue<State> _queue  = new LinkedList<>();
	
	static ParseTable _current = null;
	
	public static class Statistics{
		int _state_count = 0;
		int _rr_conflicts = 0;
		int _sr_conflicts = 0;
		
		public void print(PrintStream out){
			out.println("==== parse table statistics ====");
			out.println("state count: "+_state_count);
			out.println("conflicts count: "+(_sr_conflicts+_rr_conflicts));
			out.println(".... shift/reduce: "+_sr_conflicts);
			out.println(".... reduce/reduce: "+_rr_conflicts);
			out.println("================================");
		}
	}
	
	public ParseTable(Syntax syntax, PriorityPolicy pp, DeepReduceConflictPolicy rcp){
		_syntax = syntax;
		_ppolicy = pp;
		_rcpolicy = rcp;
		
		_states = new CollisionSet<>(syntax.symbols().count()*LOAD_FACTOR, 0.75f);
	}
	
	public ParseTable(Syntax syntax, PriorityPolicy pp){
		_syntax = syntax;
		_ppolicy = pp;
		
		_states = new CollisionSet<>(syntax.symbols().count()*LOAD_FACTOR, 0.75f);
	}
	
	public ParseTable(Syntax syntax){
		_syntax = syntax;
		
		_states = new CollisionSet<>(syntax.symbols().count()*LOAD_FACTOR, 0.75f);
	}
	
	public static ParseTable current(){
		return _current;
	}
	
	public static Symbol unique(Symbol symbol){
		return _current._syntax.symbols().get(symbol, true);
	}
	
	public static SyntaxProduction unique(SyntaxProduction prod){
		return _current._syntax.uniqueProduction(prod);
	}
	
	public static Label newLabel(Production p){
		Label l = new Label(p);
		if(_current._ppolicy == PriorityPolicy.SHALLOW || p instanceof ContextualProduction)
			_current._labels.add(l);
		return l;
	}
	
	public static LabelGroup newLabelGroup(Label l){
		LabelGroup g = new LabelGroup(l);
		_current._labels.add(g);
		return g;
	}
	
	public static LabelGroup newLabelGroup(LabelGroup copy){
		LabelGroup g = new LabelGroup(copy);
		_current._labels.add(g);
		return g;
	}
	
	public Syntax syntax(){
		return _syntax;
	}
	
	public Set<State> states(){
		return _states;
	}
	
	public PriorityPolicy priorityPolicy(){
		return _ppolicy;
	}
	
	public DeepReduceConflictPolicy deepReduceConflictPolicy(){
		return _rcpolicy;
	}
	
	public Statistics statistics(){
		Statistics st = new Statistics();
		
		st._state_count = _states.size();
		
		return st;
	}
	
	public void build() throws UndefinedSymbolException{
		_current = this;
		Benchmark.ComposedTask task = Benchmark.newComposedTask("parse table generation");
		task.start();
		
		NonTerminal start = _syntax.startProduction().product();
		
		if(_ppolicy == PriorityPolicy.DEEP){
			Benchmark.SingleTask t = task.newSingleTask("Contextual symbols");
			
			t.start();
			start = ContextualSymbol.unique(null, start, null, ContextualSymbol.Filter.NONE);
			ContextualProduction.validateAll();
			ContextualSymbol.validateAll();
			t.stop();
		}
		
		Benchmark.SingleTask t_ff = task.newSingleTask("FIRST and FOLLOW computation");
		Benchmark.SingleTask t_sg = task.newSingleTask("states generation");
		
		t_ff.start();
		_syntax.computeFollowSets();
		t_ff.stop();
		
		/*if(_ppolicy == PriorityPolicy.DEEP){
			//_syntax.symbols().computeClasses();
			ContextualProduction.createAllLabels();
		}*/ 
		
		t_sg.start();
		State s0 = new State(this);
		
		for(Production p : start.productions()){
			s0.addItem(new Item(s0.items(), p));
		}
		
		addState(s0);
		processQueue();
		t_sg.stop();
		
		task.stop();
		_current = null;
	}
	
	public State addState(State state) throws UndefinedSymbolException{
		state.complete();
		
		/*for(State s : _states){ // Avoid state duplication
			if(s.equals(state)){
				if(_rcpolicy == DeepReduceConflictPolicy.MERGE)
					s.merge(state);
				return s;
			}
		}*/
		State s = _states.push(state);
		if(s != null){
			if(_rcpolicy == DeepReduceConflictPolicy.MERGE)
				s.merge(state);
			return s;
		}
		
		state.close();
		state.assignId(_states.size()-1);
		state.reduce();
		_queue.add(state);
		
		return state;
	}
	
	public void processQueue() throws UndefinedSymbolException{
		while(!_queue.isEmpty()){
			while(!_queue.isEmpty()){
				State state = _queue.poll();
				state.shift();
			}
			
			/*for(State state : _states){
				
			}*/
			
			/*for(State state : _states){ // TODO do it in reduce phase
				if(state.shiftPending()){
					_queue.add(state);
				}
			}*/
		}
	}
	
	public static ParseTable fromSyntax(Syntax syntax, PriorityPolicy pp) throws UndefinedSymbolException{
		ParseTable table = new ParseTable(syntax, pp);
		
		table.build();
		
		return table;
	}
	
	public static void fromFile(File input, File output, List<String> paths){
		Benchmark.reset();
		
		Benchmark.SingleTask t_import = Benchmark.main.newSingleTask("import");
    	Benchmark.ComposedTask t_generate = Benchmark.main.newComposedTask("generation");
    	Benchmark.SingleTask t_export = Benchmark.main.newSingleTask("export");
		
    	// import modules
    	t_import.start();
		Syntax syntax = null;
		try{
			syntax = Module.fromFile(input, paths);
		}catch (ModuleNotFoundException e){
			System.err.println(e.getMessage());
			return;
		}
		t_import.stop();
		
		// generate parse table
		t_generate.start();
		ParseTable pt = null;
		try {
			pt = ParseTable.fromSyntax(syntax, PriorityPolicy.DEEP);
		} catch (UndefinedSymbolException e) {
			System.err.println(e.getMessage());
			return;
		}
		t_generate.stop();
		
		// export parse table
		t_export.start();
		IStrategoTerm result = pt.toATerm();
        if(output != null){
	        FileWriter out = null;
	        try{
				out = new FileWriter(output);
				
				out.write(result.toString());
				
				out.close();
			}catch (IOException e){
				System.err.println(e.getMessage());
			}
        }else{
        	System.out.println(result.toString());
        }
        t_export.stop();
        
        //pt.generateGraphvizFile(java.nio.file.Paths.get(output.getPath()+".dot"));
        
        pt.statistics().print(System.err);
        Benchmark.print(System.err);
        State.reset();
        Label.reset();
	}
	
	void generateGraphvizFile(Path file){
    	try {
			Files.write(file, digraph().getBytes());
			System.err.println("Graphiv written at: "+file.toAbsolutePath().toUri().getPath());
		} catch (IOException e){
			System.err.println(e.getMessage());
		}
    }
	
	public int getInitialState(){
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	public IStrategoTerm toATerm(){
		_current = this;
		
		ArrayList<IStrategoTerm> labels = new ArrayList<>();
		ArrayList<IStrategoTerm> priorities = new ArrayList<>();
		
		for(Label l : _labels){
			labels.add(l.toATerm());
		}
		
		for(SyntaxProduction p : _syntax.productions()){
			labels.add(p.label().toATerm()); // to match priorities
			priorities.addAll(p.priorities().toATerms());
		}
		
		_current = null;
		
		return new StrategoAppl(
				CONS_PT,
				new IStrategoTerm[]{
						new StrategoInt(VERSION, null, 0),
						new StrategoInt(getInitialState(), null, 0),
						Utilities.strategoListFromCollection(labels),
						new StrategoAppl(
								CONS_STATES,
								new IStrategoTerm[]{
										Utilities.strategoListFromExportables((Collection<Exportable>)(Object)_states)
								},
								null,
								0
						),
						new StrategoAppl(
								CONS_PRIORITIES,
								new IStrategoTerm[]{
										Utilities.strategoListFromCollection(priorities)
								},
								null,
								0
						)
				},
				null,
				0
		);
	}
	
	public String digraph(){
		String str = "digraph g{\n";
		str += "graph [rankdir = \"LR\"];\n";
		
		for(State s : _states){
			str += s.digraph();
		}
		
		str += "}";
		return str;
	}

	public static void declareContextualProduction(ContextualProduction contextualProduction) {
		_current._syntax.declareContextualProduction(contextualProduction);
	}
}
