package org.metaborg.parsetable.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.metaborg.parsetable.actions.IAction;
import org.metaborg.parsetable.actions.IReduce;

public final class ActionsForCharacterSeparated implements IActionsForCharacter, Serializable {

    private static final long serialVersionUID = -3200862105789432300L;

    private final ActionForCharacterClass[] actions;
    private final ActionForCharacterClass[] recoveryActions;

    public ActionsForCharacterSeparated(ActionsPerCharacterClass[] actionsPerCharacterClasses,
        Set<Integer> recoveryStateIds) {
        actions = mapActions(filterNonRecoveryActions(actionsPerCharacterClasses, recoveryStateIds));
        recoveryActions = mapActions(actionsPerCharacterClasses);
    }

    private ActionForCharacterClass[] mapActions(ActionsPerCharacterClass[] actionsPerCharacterClasses) {
        List<ActionForCharacterClass> actionPerCharacterClasses = new ArrayList<>();

        for(ActionsPerCharacterClass actionsPerCharacterClass : actionsPerCharacterClasses) {
            for(IAction action : actionsPerCharacterClass.actions)
                actionPerCharacterClasses
                    .add(new ActionForCharacterClass(actionsPerCharacterClass.characterClass, action));
        }

        ActionForCharacterClass[] actions = new ActionForCharacterClass[actionPerCharacterClasses.size()];

        actionPerCharacterClasses.toArray(actions);

        return actions;
    }

    @Override public IAction[] getActions() {
        IAction[] res = new IAction[actions.length];

        for(int i = 0; i < actions.length; i++)
            res[i] = actions[i].action;

        return res;
    }

    @Override public Iterable<IAction> getApplicableActions(IActionQuery actionQuery, ParsingMode mode) {
        ActionForCharacterClass[] actions = mode == ParsingMode.Recovery ? this.recoveryActions : this.actions;

        return () -> new Iterator<IAction>() {
            int index = 0;

            @Override public boolean hasNext() {
                while(index < actions.length && !(actions[index].appliesTo(actionQuery.actionQueryCharacter())
                    && actions[index].action.allowsLookahead(actionQuery))) {
                    index++;
                }
                return index < actions.length;
            }

            @Override public IAction next() {
                return actions[index++].action;
            }
        };
    }

    @Override public Iterable<IReduce> getApplicableReduceActions(IActionQuery actionQuery, ParsingMode mode) {
        ActionForCharacterClass[] actions = mode == ParsingMode.Recovery ? this.recoveryActions : this.actions;

        return () -> new Iterator<IReduce>() {
            int index = 0;

            @Override public boolean hasNext() {
                while(index < actions.length && !(actions[index].appliesTo(actionQuery.actionQueryCharacter())
                    && actions[index].action.isApplicableReduce(actionQuery))) {
                    index++;
                }
                return index < actions.length;
            }

            @Override public IReduce next() {
                return (IReduce) actions[index++].action;
            }
        };
    }

}
