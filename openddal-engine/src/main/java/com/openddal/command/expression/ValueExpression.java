/*
 * Copyright 2014-2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.openddal.command.expression;

import java.util.List;

import com.openddal.dbobject.index.IndexCondition;
import com.openddal.dbobject.table.ColumnResolver;
import com.openddal.dbobject.table.TableFilter;
import com.openddal.engine.Session;
import com.openddal.message.DbException;
import com.openddal.value.Value;
import com.openddal.value.ValueArray;
import com.openddal.value.ValueBoolean;
import com.openddal.value.ValueNull;

/**
 * An expression representing a constant value.
 */
public class ValueExpression extends Expression {
    /**
     * The expression represents ValueNull.INSTANCE.
     */
    private static final Object NULL = new ValueExpression(ValueNull.INSTANCE);

    /**
     * This special expression represents the default value. It is used for
     * UPDATE statements of the form SET COLUMN = DEFAULT. The value is
     * ValueNull.INSTANCE, but should never be accessed.
     */
    private static final Object DEFAULT = new ValueExpression(ValueNull.INSTANCE);

    private final Value value;

    private ValueExpression(Value value) {
        this.value = value;
    }

    /**
     * Get the NULL expression.
     *
     * @return the NULL expression
     */
    public static ValueExpression getNull() {
        return (ValueExpression) NULL;
    }

    /**
     * Get the DEFAULT expression.
     *
     * @return the DEFAULT expression
     */
    public static ValueExpression getDefault() {
        return (ValueExpression) DEFAULT;
    }

    /**
     * Create a new expression with the given value.
     *
     * @param value the value
     * @return the expression
     */
    public static ValueExpression get(Value value) {
        if (value == ValueNull.INSTANCE) {
            return getNull();
        }
        return new ValueExpression(value);
    }

    @Override
    public Value getValue(Session session) {
        return value;
    }

    @Override
    public int getType() {
        return value.getType();
    }

    @Override
    public void createIndexConditions(Session session, TableFilter filter) {
        if (value.getType() == Value.BOOLEAN) {
            boolean v = value.getBoolean().booleanValue();
            if (!v) {
                filter.addIndexCondition(IndexCondition.get(Comparison.FALSE, null, this));
            }
        }
    }

    @Override
    public Expression getNotIfPossible(Session session) {
        return new Comparison(session, Comparison.EQUAL, this,
                ValueExpression.get(ValueBoolean.get(false)));
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        // nothing to do
    }

    @Override
    public Expression optimize(Session session) {
        return this;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isValueSet() {
        return true;
    }

    @Override
    public void setEvaluatable(TableFilter tableFilter, boolean b) {
        // nothing to do
    }

    @Override
    public int getScale() {
        return value.getScale();
    }

    @Override
    public long getPrecision() {
        return value.getPrecision();
    }

    @Override
    public int getDisplaySize() {
        return value.getDisplaySize();
    }

    @Override
    public String getSQL() {
        if (this == DEFAULT) {
            return "DEFAULT";
        }
        return value.getSQL();
    }

    @Override
    public void updateAggregate(Session session) {
        // nothing to do
    }

    @Override
    public boolean isEverything(ExpressionVisitor visitor) {
        switch (visitor.getType()) {
            case ExpressionVisitor.DETERMINISTIC:
            case ExpressionVisitor.READONLY:
            case ExpressionVisitor.INDEPENDENT:
            case ExpressionVisitor.EVALUATABLE:
            case ExpressionVisitor.NOT_FROM_RESOLVER:
            case ExpressionVisitor.GET_DEPENDENCIES:
            case ExpressionVisitor.QUERY_COMPARABLE:
            case ExpressionVisitor.GET_COLUMNS:
                return true;
            default:
                throw DbException.throwInternalError("type=" + visitor.getType());
        }
    }

    @Override
    public int getCost() {
        return 0;
    }

    @Override
    public Expression[] getExpressionColumns(Session session) {
        if (getType() == Value.ARRAY) {
            return getExpressionColumns(session, (ValueArray) getValue(session));
        }
        return super.getExpressionColumns(session);
    }

    @Override
    public String exportParameters(TableFilter filter, List<Value> container) {
        if (this == DEFAULT) {
            return "DEFAULT";
        }
        container.add(value);
        return "?";
    }
}
