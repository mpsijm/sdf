package org.metaborg.sdf2table.parsetable;

import java.io.Serializable;
import java.util.*;

import org.metaborg.parsetable.actions.IAction;
import org.metaborg.parsetable.actions.IGoto;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.characterclasses.CharacterClassFactory;
import org.metaborg.parsetable.characterclasses.ICharacterClass;
import org.metaborg.parsetable.query.*;
import org.metaborg.parsetable.states.IState;
import org.metaborg.sdf2table.grammar.CharacterClassSymbol;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.ISymbol;
import org.metaborg.sdf2table.grammar.Symbol;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class State implements IState, Comparable<State>, Serializable {

    private static final long serialVersionUID = 7118071460461287164L;

    ParseTable pt;

    private final int label;
    private Set<Goto> gotos;
    private Map<Integer, IGoto> gotosMapping;
    private final Set<LRItem> kernel;
    private Set<LRItem> items;
    private LinkedHashMultimap<Symbol, LRItem> symbol_items;
    private LinkedHashMultimap<ICharacterClass, Action> lr_actions;
    IActionsForCharacter actionsForCharacter;
    private boolean rejectable;

    private StateStatus status = StateStatus.VISIBLE;

    public Set<State> states = Sets.newHashSet();

    public State(IProduction p, ParseTable pt) {
        items = Sets.newLinkedHashSet();
        gotos = Sets.newLinkedHashSet();
        gotosMapping = Maps.newHashMap();
        kernel = Sets.newLinkedHashSet();
        symbol_items = LinkedHashMultimap.create();
        lr_actions = LinkedHashMultimap.create();
        this.rejectable = false;

        this.pt = pt;
        label = this.pt.totalStates();
        this.pt.stateLabels().put(label, this);
        this.pt.incTotalStates();

        LRItem item = new LRItem(p, 0, pt);
        kernel.add(item);
        pt.kernelMap().put(kernel, this);
    }

    public State(Set<LRItem> kernel, ParseTable pt) {
        items = Sets.newLinkedHashSet();
        gotos = Sets.newLinkedHashSet();
        gotosMapping = Maps.newHashMap();
        symbol_items = LinkedHashMultimap.create();
        lr_actions = LinkedHashMultimap.create();
        this.rejectable = false;

        this.kernel = Sets.newLinkedHashSet();
        this.kernel.addAll(kernel);
        pt.kernelMap().put(kernel, this);

        this.pt = pt;
        label = this.pt.totalStates();
        this.pt.stateLabels().put(label, this);
        this.pt.incTotalStates();
    }

    public void closure() {
        for(LRItem item : kernel) {
            // if(item.getDotPosition() < item.getProd().arity()) {
            // pt.symbolStatesMapping().put(item.getProd().rightHand().get(item.getDotPosition()), this);
            // }
            item.process(items, symbol_items, this);
        }
    }

    public void doShift() {
        for(Symbol s_at_dot : symbol_items.keySet()) {
            if(s_at_dot instanceof CharacterClassSymbol) {
                Set<LRItem> new_kernel = Sets.newLinkedHashSet();
                Set<Goto> new_gotos = Sets.newLinkedHashSet();
                Set<Shift> new_shifts = Sets.newLinkedHashSet();
                for(LRItem item : symbol_items.get(s_at_dot)) {
                    Shift shift = new Shift(((CharacterClassSymbol) s_at_dot).getCC());
                    new_kernel.add(item.shiftDot());
                    if(!(item.getProd().equals(pt.initialProduction()) && item.getDotPosition() == 1)) {
                        new_shifts.add(shift);
                    }
                }
                if(!new_kernel.isEmpty()) {
                    checkKernel(new_kernel, new_gotos, new_shifts);
                }
            } else {
                for(IProduction p : pt.normalizedGrammar().getSymbolProductionsMapping().get(s_at_dot)) {

                    // p might be a contextual production
                    if(pt.normalizedGrammar().getProdContextualProdMapping().get(p) != null) {
                        p = pt.normalizedGrammar().getProdContextualProdMapping().get(p);
                    }

                    Set<LRItem> new_kernel = Sets.newLinkedHashSet();
                    Set<Goto> new_gotos = Sets.newLinkedHashSet();
                    Set<Shift> new_shifts = Sets.newLinkedHashSet();
                    for(LRItem item : symbol_items.get(s_at_dot)) {
                        // if item.prod does not conflict with p
                        if(!item.isPriorityConflict(p)) {
                            new_kernel.add(item.shiftDot());
                            new_gotos.add(new Goto(pt.productionLabels().get(p), pt));
                        }
                    }
                    if(!new_kernel.isEmpty()) {
                        checkKernel(new_kernel, new_gotos, new_shifts);
                    }
                }
            }
        }
    }

    public void doReduces() {
        // for each item p_i : A = A0 ... AN .
        // add a reduce action reduce([0-MAX_CHAR,eof] / follow(A), p_i)
        for(LRItem item : items) {

            if(item.getDotPosition() == item.getProd().arity()) {
                int prod_label = pt.productionLabels().get(item.getProd());

                ISymbol leftHandSymbol = item.getProd().leftHand();
                ICharacterClass fr = leftHandSymbol.followRestriction();
                if((fr == null || fr.isEmpty()) && leftHandSymbol.followRestrictionLookahead() == null) {
                    addReduceAction(item.getProd(), prod_label, CharacterClassFactory.FULL_RANGE, null);
                } else {
                    ICharacterClass final_range = CharacterClassFactory.FULL_RANGE;
                    // Not based on first and follow sets thus, only considering the follow restrictions
                    if(fr != null && !fr.isEmpty()) {
                        final_range = final_range.difference(leftHandSymbol.followRestriction());
                    }
                    for(ICharacterClass[] s : leftHandSymbol.followRestrictionLookahead()) {
                        final_range = final_range.difference(s[0]);

                        // create reduce Lookahead actions
                        ICharacterClass[] lookahead = Arrays.copyOfRange(s, 1, s.length);
                        addReduceAction(item.getProd(), prod_label, s[0], lookahead);
                    }
                    addReduceAction(item.getProd(), prod_label, final_range, null);
                }
            }
            // <Start> = <START> . <EOF>
            if(item.getProd().equals(pt.initialProduction()) && item.getDotPosition() == 1) {
                lr_actions.put(CharacterClassFactory.EOF_SINGLETON, new Accept(CharacterClassFactory.EOF_SINGLETON));
            }
        }
    }

    private void addReduceAction(IProduction p, Integer label, ICharacterClass cc, ICharacterClass[] lookahead) {
        ICharacterClass final_range = cc;
        ParseTableProduction prod = pt.productionsMapping().get(p);

        LinkedHashMultimap<ICharacterClass, Action> newLR_actions = LinkedHashMultimap.create();

        for(ICharacterClass range : lr_actions.keySet()) {
            if(final_range.isEmpty()) {
                break;
            }
            ICharacterClass intersection = final_range.intersection(range);
            if(!intersection.isEmpty()) {
                if(intersection.equals(range)) {
                    if(lookahead != null) {
                        newLR_actions.put(intersection, new ReduceLookahead(prod, label, intersection, lookahead));
                    } else {
                        newLR_actions.put(intersection, new Reduce(prod, label, intersection));
                    }
                    final_range = final_range.difference(intersection);
                }
            }
        }

        lr_actions.putAll(newLR_actions);

        if(!final_range.isEmpty()) {
            if(lookahead != null) {
                lr_actions.put(final_range, new ReduceLookahead(prod, label, final_range, lookahead));
            } else {
                lr_actions.put(final_range, new Reduce(prod, label, final_range));
            }
        }
    }

    private void checkKernel(Set<LRItem> new_kernel, Set<Goto> new_gotos, Set<Shift> new_shifts) {
        State gotoState;
        if(pt.kernelMap().containsKey(new_kernel)) {
            gotoState = pt.kernelMap().get(new_kernel);
        } else {
            gotoState = new State(new_kernel, pt);
            gotoState.closure();
            pt.stateQueue().add(gotoState);
        }
        int stateNumber = gotoState.getLabel();

        // set recently added shift and goto actions to new state
        for(Shift shift : new_shifts) {
            shift.setState(stateNumber);
            ICharacterClass shiftCC = shift.cc;
            List<Map.Entry<ICharacterClass, Action>> shiftActionsInSameRange = getShiftActionsInSameRange(shiftCC);
            if(!shiftActionsInSameRange.isEmpty()) {
                for(Map.Entry<ICharacterClass, Action> entry : shiftActionsInSameRange) {
                    ICharacterClass intersection = entry.getKey().intersection(shiftCC);
                    if(intersection.isEmpty())
                        continue;

                    this.lr_actions.remove(entry.getKey(), entry.getValue());

                    ICharacterClass difference = entry.getKey().difference(shiftCC);
                    if(!difference.isEmpty())
                        this.lr_actions.put(difference, entry.getValue());

                    Set<LRItem> mergedKernel = new HashSet<>();
                    mergedKernel.addAll(gotoState.kernel);
                    mergedKernel.addAll(pt.stateLabels().get(((Shift) entry.getValue()).getState()).kernel);
                    State mergedState = new State(mergedKernel, pt);
                    mergedState.closure();
                    pt.stateQueue().add(mergedState);
                    Shift new_shift = new Shift(intersection);
                    new_shift.setState(mergedState.label);
                    this.lr_actions.put(intersection, new_shift);

                    shiftCC = shiftCC.difference(entry.getKey());
                    if(shiftCC.isEmpty())
                        break;
                }
            }
            if(!shiftCC.isEmpty())
                this.lr_actions.put(shiftCC, shift);
        }
        for(Goto g : new_gotos) {
            g.setState(stateNumber);
            this.gotos.add(g);
            this.gotosMapping.put(g.label, g);
        }
    }

    private List<Map.Entry<ICharacterClass, Action>> getShiftActionsInSameRange(ICharacterClass cc) {
        List<Map.Entry<ICharacterClass, Action>> sameRangeActions = new ArrayList<>();
        for(Map.Entry<ICharacterClass, Action> entry : lr_actions.entries()) {
            if(entry.getValue() instanceof Shift && !entry.getKey().intersection(cc).isEmpty()) {
                sameRangeActions.add(entry);
            }
        }
        return sameRangeActions;
    }

    @Override public String toString() {
        String buf = "";
        int i = 0;
        buf += "State " + getLabel();
        if(!gotos.isEmpty()) {
            buf += "\nGotos: ";
        }
        for(IGoto g : gotos) {
            if(i != 0)
                buf += "\n     , ";
            buf += g;
            i++;
        }
        if(!lr_actions.isEmpty()) {
            buf += "\nActions: ";
        }
        i = 0;
        for(ICharacterClass cc : lr_actions.keySet()) {
            if(i != 0)
                buf += "\n       , ";
            buf += cc + ": ";
            int j = 0;
            for(IAction a : lr_actions.get(cc)) {
                if(j != 0)
                    buf += ", ";
                buf += a;
                j++;
            }
            i++;
        }
        if(!items.isEmpty()) {
            buf += "\nItems: ";

            i = 0;
            for(LRItem it : items) {
                if(i != 0)
                    buf += "\n       ";
                buf += it.toString();
                i++;
            }
        } else {
            buf += "\nItems: ";

            i = 0;
            for(LRItem it : kernel) {
                if(i != 0)
                    buf += "\n       ";
                buf += it.toString();
                i++;
            }
        }

        return buf;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((kernel == null) ? 0 : kernel.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if(kernel == null) {
            if(other.kernel != null)
                return false;
        } else if(!kernel.equals(other.kernel))
            return false;
        return true;
    }

    @Override public int compareTo(State o) {
        return this.getLabel() - o.getLabel();
    }

    public int getLabel() {
        return label;
    }

    public Set<LRItem> getItems() {
        return items;
    }

    public StateStatus status() {
        return status;
    }

    public void setStatus(StateStatus status) {
        this.status = status;
    }

    public void markDirty() {
        this.items.clear();
        this.symbol_items.clear();
        this.lr_actions.clear();
        this.setStatus(StateStatus.DIRTY);
    }

    public Set<Goto> gotos() {
        return gotos;
    }

    public Iterable<Action> actions() {
        Set<Action> actions = Sets.newHashSet(lr_actions.values());
        return actions;
    }

    public SetMultimap<ICharacterClass, Action> actionsMapping() {
        return lr_actions;
    }

    @Override public boolean isRejectable() {
        return rejectable;
    }

    public void markRejectable() {
        this.rejectable = true;
    }

    @Override public int id() {
        return label;
    }

    @Override public Iterable<IAction> getApplicableActions(IActionQuery actionQuery, ParsingMode parsingMode) {
        return actionsForCharacter.getApplicableActions(actionQuery, parsingMode);
    }

    @Override public Iterable<IReduce> getApplicableReduceActions(IActionQuery actionQuery, ParsingMode parsingMode) {
        return actionsForCharacter.getApplicableReduceActions(actionQuery, parsingMode);
    }

    @Override public int getGotoId(int productionId) {
        return gotosMapping.get(productionId).gotoStateId();
    }

    public void calculateActionsForCharacter() {
        // TODO: this should take into account which states only contain recovery reduces
        actionsForCharacter = new ActionsForCharacterDisjointSorted(readActions(), Collections.emptySet());
    }

    private ActionsPerCharacterClass[] readActions() {
        Set<ICharacterClass> characterClasses = lr_actions.keySet();

        ActionsPerCharacterClass[] actionsPerCharacterClasses = new ActionsPerCharacterClass[characterClasses.size()];

        int i = 0;
        for(ICharacterClass cc : characterClasses) {
            actionsPerCharacterClasses[i++] =
                new ActionsPerCharacterClass(cc, lr_actions.get(cc).toArray(new IAction[0]));
        }

        return actionsPerCharacterClasses;
    }


}
