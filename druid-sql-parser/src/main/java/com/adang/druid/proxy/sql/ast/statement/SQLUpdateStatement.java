/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adang.druid.proxy.sql.ast.statement;


import com.adang.druid.proxy.sql.SQLUtils;
import com.adang.druid.proxy.sql.ast.SQLExpr;
import com.adang.druid.proxy.sql.ast.SQLName;
import com.adang.druid.proxy.sql.ast.SQLObject;
import com.adang.druid.proxy.sql.ast.SQLOrderBy;
import com.adang.druid.proxy.sql.ast.SQLReplaceable;
import com.adang.druid.proxy.sql.ast.SQLStatementImpl;
import com.adang.druid.proxy.sql.ast.expr.SQLBinaryOpExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLBinaryOpExprGroup;
import com.adang.druid.proxy.sql.ast.expr.SQLBinaryOperator;
import com.adang.druid.proxy.sql.visitor.SQLASTOutputVisitor;
import com.adang.druid.proxy.sql.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class SQLUpdateStatement extends SQLStatementImpl implements SQLReplaceable {
    protected SQLWithSubqueryClause with; // for pg

    protected final List<SQLUpdateSetItem> items = new ArrayList<SQLUpdateSetItem>();
    protected SQLExpr where;
    protected SQLTableSource from;

    protected SQLTableSource tableSource;
    protected List<SQLExpr>                returning;

    // for mysql
    protected SQLOrderBy orderBy;

    public SQLUpdateStatement(){

    }
    
    public SQLUpdateStatement(String dbType){
        super (dbType);
    }

    public SQLTableSource getTableSource() {
        return tableSource;
    }

    public void setTableSource(SQLExpr expr) {
        this.setTableSource(new SQLExprTableSource(expr));
    }

    public void setTableSource(SQLTableSource tableSource) {
        if (tableSource != null) {
            tableSource.setParent(this);
        }
        this.tableSource = tableSource;
    }

    public SQLName getTableName() {
        if (tableSource instanceof SQLExprTableSource) {
            return ((SQLExprTableSource) tableSource).getName();
        }

        if (tableSource instanceof SQLJoinTableSource) {
            SQLTableSource left = ((SQLJoinTableSource) tableSource).getLeft();
            if (left instanceof SQLExprTableSource) {
                return ((SQLExprTableSource) left).getName();
            }
        }
        return null;
    }

    public SQLExpr getWhere() {
        return where;
    }

    public void setWhere(SQLExpr where) {
        if (where != null) {
            where.setParent(this);
        }
        this.where = where;
    }

    public List<SQLUpdateSetItem> getItems() {
        return items;
    }
    
    public void addItem(SQLUpdateSetItem item) {
        this.items.add(item);
        item.setParent(this);
    }

    public List<SQLExpr> getReturning() {
        if (returning == null) {
            returning = new ArrayList<SQLExpr>(2);
        }

        return returning;
    }

    public SQLTableSource getFrom() {
        return from;
    }

    public void setFrom(SQLTableSource from) {
        if (from != null) {
            from.setParent(this);
        }
        this.from = from;
    }

    @Override
    public void output(StringBuffer buf) {
        SQLASTOutputVisitor visitor = SQLUtils.createOutputVisitor(buf, dbType);
        this.accept(visitor);
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, tableSource);
            acceptChild(visitor, from);
            acceptChild(visitor, items);
            acceptChild(visitor, where);
            acceptChild(visitor, orderBy);
        }
        visitor.endVisit(this);
    }

    public List<SQLObject> getChildren() {
        List<SQLObject> children = new ArrayList<SQLObject>();
        if (tableSource != null) {
            children.add(tableSource);
        }
        if (from != null) {
            children.add(from);
        }
        children.addAll(this.items);
        if (where != null) {
            children.add(where);
        }
        if (orderBy != null) {
            children.add(orderBy);
        }
        return children;
    }

    @Override
    public boolean replace(SQLExpr expr, SQLExpr target) {
        if (where == expr) {
            setWhere(target);
            return true;
        }
        return false;
    }


    public SQLOrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(SQLOrderBy orderBy) {
        if (orderBy != null) {
            orderBy.setParent(this);
        }
        this.orderBy = orderBy;
    }

    public SQLWithSubqueryClause getWith() {
        return with;
    }

    public void setWith(SQLWithSubqueryClause with) {
        if (with != null) {
            with.setParent(this);
        }
        this.with = with;
    }

    public void addCondition(String conditionSql) {
        if (conditionSql == null || conditionSql.length() == 0) {
            return;
        }

        SQLExpr condition = SQLUtils.toSQLExpr(conditionSql, dbType);
        addCondition(condition);
    }

    public void addCondition(SQLExpr expr) {
        if (expr == null) {
            return;
        }

        this.setWhere(SQLBinaryOpExpr.and(where, expr));
    }

    public boolean removeCondition(String conditionSql) {
        if (conditionSql == null || conditionSql.length() == 0) {
            return false;
        }

        SQLExpr condition = SQLUtils.toSQLExpr(conditionSql, dbType);

        return removeCondition(condition);
    }

    public boolean removeCondition(SQLExpr condition) {
        if (condition == null) {
            return false;
        }

        if (where instanceof SQLBinaryOpExprGroup) {
            SQLBinaryOpExprGroup group = (SQLBinaryOpExprGroup) where;

            int removedCount = 0;
            List<SQLExpr> items = group.getItems();
            for (int i = items.size() - 1; i >= 0; i--) {
                if (items.get(i).equals(condition)) {
                    items.remove(i);
                    removedCount++;
                }
            }
            if (items.size() == 0) {
                where = null;
            }

            return removedCount > 0;
        }

        if (where instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpWhere = (SQLBinaryOpExpr) where;
            SQLBinaryOperator operator = binaryOpWhere.getOperator();
            if (operator == SQLBinaryOperator.BooleanAnd || operator == SQLBinaryOperator.BooleanOr) {
                List<SQLExpr> items = SQLBinaryOpExpr.split(binaryOpWhere);

                int removedCount = 0;
                for (int i = items.size() - 1; i >= 0; i--) {
                    SQLExpr item = items.get(i);
                    if (item.equals(condition)) {
                        if (SQLUtils.replaceInParent(item, null)) {
                            removedCount++;
                        }
                    }
                }

                return removedCount > 0;
            }
        }

        if (condition.equals(where)) {
            where = null;
            return true;
        }

        return false;
    }
}
