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
package com.adang.druid.proxy.sql.dialect.mysql.ast.statement;


import com.adang.druid.proxy.sql.ast.SQLCommentHint;
import com.adang.druid.proxy.sql.ast.SQLName;
import com.adang.druid.proxy.sql.ast.statement.SQLExprTableSource;
import com.adang.druid.proxy.sql.dialect.mysql.visitor.MySqlASTVisitor;

import java.util.List;

public class MySqlLockTableStatement extends MySqlStatementImpl {

    private SQLExprTableSource tableSource;

    private LockType           lockType;
    
    private List<SQLCommentHint> hints;

    public SQLExprTableSource getTableSource() {
        return tableSource;
    }

    public void setTableSource(SQLExprTableSource tableSource) {
        if (tableSource != null) {
            tableSource.setParent(this);
        }
        this.tableSource = tableSource;
    }

    public void setTableSource(SQLName name) {
        setTableSource(new SQLExprTableSource(name));
    }

    public LockType getLockType() {
        return lockType;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public void accept0(MySqlASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, tableSource);
        }
        visitor.endVisit(this);
    }

    public static enum LockType {
        READ("READ"), READ_LOCAL("READ LOCAL"), WRITE("WRITE"), LOW_PRIORITY_WRITE("LOW_PRIORITY WRITE");

        public final String name;

        LockType(String name){
            this.name = name;
        }
    }

    public List<SQLCommentHint> getHints() {
        return hints;
    }

    public void setHints(List<SQLCommentHint> hints) {
        this.hints = hints;
    }
}
