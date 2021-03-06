/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openjpa.jdbc.kernel.exps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Tests whether a value is IN a collection.
 *
 * @author Abe White
 */
class InExpression
    implements Exp {

    private final Val _val;
    private final Const _const;

    /**
     * Constructor. Supply the value to test and the constant to obtain
     * the parameters from.
     */
    public InExpression(Val val, Const constant) {
        _val = val;
        _const = constant;
    }

    /**
     * Constant collection.
     */
    public Const getConstant() {
        return _const;
    }

    /**
     * Contained value.
     */
    public Val getValue() {
        return _val;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        ExpState valueState = _val.initialize(sel, ctx, 0);
        ExpState constantState = _const.initialize(sel, ctx, 0);
        return new InExpState(valueState.joins, constantState, valueState);
    }

    /**
     * Expression state.
     */
    private static class InExpState
        extends ExpState {

        public final ExpState constantState;
        public final ExpState valueState;

        public InExpState(Joins joins, ExpState constantState, 
            ExpState valueState) {
            super(joins);
            this.constantState = constantState;
            this.valueState = valueState;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        InExpState istate = (InExpState) state; 
        _const.calculateValue(sel, ctx, istate.constantState, null, null);
        _val.calculateValue(sel, ctx, istate.valueState, null, null);

        Collection coll = getCollection(ctx, istate.constantState);
        if (coll != null) {
            Collection ds = new ArrayList(coll.size());
            for (Iterator itr = coll.iterator(); itr.hasNext();)
                ds.add(_val.toDataStoreValue(sel, ctx, istate.valueState, 
                    itr.next()));
            coll = ds;
        }

        Column[] cols = null;
        if (_val instanceof PCPath)
            cols = ((PCPath) _val).getColumns(istate.valueState);
        else if (_val instanceof GetObjectId)
            cols = ((GetObjectId) _val).getColumns(istate.valueState);

        if (coll == null || coll.isEmpty())
            buf.append("1 <> 1");
        else if (_val.length(sel, ctx, istate.valueState) == 1)
            inContains(sel, ctx, istate.valueState, buf, coll, cols);
        else
            orContains(sel, ctx, istate.valueState, buf, coll, cols);
        sel.append(buf, state.joins);
    }

    /**
     * Construct an IN clause with the value of the given collection.
     */
    private void inContains(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf, Collection coll, Column[] cols) {
        _val.appendTo(sel, ctx, state, buf, 0);
        buf.append(" IN (");

        Column col = (cols != null && cols.length == 1) ? cols[0] : null;
        for (Iterator itr = coll.iterator(); itr.hasNext();) {
            buf.appendValue(itr.next(), col);
            if (itr.hasNext())
                buf.append(", ");
        }
        buf.append(")");
    }

    /**
     * If the value to test is a compound key, we can't use IN,
     * so create a clause like '(a = b AND c = d) OR (e = f AND g = h) ...'
     */
    private void orContains(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf, Collection coll, Column[] cols) {
        if (coll.size() > 1)
            buf.append("(");

        Object[] vals;
        Column col;
        for (Iterator itr = coll.iterator(); itr.hasNext();) {
            vals = (Object[]) itr.next();

            buf.append("(");
            for (int i = 0; i < vals.length; i++) {
                col = (cols != null && cols.length == vals.length)
                    ? cols[i] : null;
                if (i > 0)
                    buf.append(" AND ");

                _val.appendTo(sel, ctx, state, buf, i);
                if (vals[i] == null)
                    buf.append(" IS ");
                else
                    buf.append(" = ");
                buf.appendValue(vals[i], col);
            }
            buf.append(")");

            if (itr.hasNext())
                buf.append(" OR ");
        }
        if (coll.size() > 1)
            buf.append(")");
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        InExpState istate = (InExpState) state; 
        _const.selectColumns(sel, ctx, istate.constantState, true);
        _val.selectColumns(sel, ctx, istate.valueState, true);
    }

    /**
     * Return the collection to test for containment with.
     */
    protected Collection getCollection(ExpContext ctx, ExpState state) {
        return (Collection) _const.getValue(ctx, state);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        _const.acceptVisit(visitor);
        visitor.exit(this);
    }
}
