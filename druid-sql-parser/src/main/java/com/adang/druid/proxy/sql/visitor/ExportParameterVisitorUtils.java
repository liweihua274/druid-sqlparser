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
package com.adang.druid.proxy.sql.visitor;



import com.adang.druid.proxy.sql.ast.SQLExpr;
import com.adang.druid.proxy.sql.ast.SQLObject;
import com.adang.druid.proxy.sql.ast.expr.SQLBetweenExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLBinaryOpExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLBooleanExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLCharExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLHexExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLLiteralExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLNumericLiteralExpr;
import com.adang.druid.proxy.sql.ast.expr.SQLVariantRefExpr;
import com.adang.druid.proxy.sql.dialect.mysql.visitor.MySqlExportParameterVisitor;
import com.adang.druid.proxy.util.JdbcUtils;

import java.util.ArrayList;
import java.util.List;

public final class ExportParameterVisitorUtils {
    
    //private for util class not need new instance
    private ExportParameterVisitorUtils() {
        super();
    }

    public static ExportParameterVisitor createExportParameterVisitor(final  Appendable out , final String dbType) {
        
        if (JdbcUtils.MYSQL.equals(dbType)) {
            return new MySqlExportParameterVisitor(out);
        }

        
        if (JdbcUtils.MARIADB.equals(dbType)) {
            return new MySqlExportParameterVisitor(out);
        }
        
        if (JdbcUtils.H2.equals(dbType)) {
            return new MySqlExportParameterVisitor(out);
        }

       return new ExportParameterizedOutputVisitor(out);
    }

    

    public static boolean exportParamterAndAccept(final List<Object> parameters, List<SQLExpr> list) {
        for (int i = 0, size = list.size(); i < size; ++i) {
            SQLExpr param = list.get(i);

            SQLExpr result = exportParameter(parameters, param);
            if (result != param) {
                list.set(i, result);
            }
        }

        return false;
    }

    public static SQLExpr exportParameter(final List<Object> parameters, final SQLExpr param) {
        Object value = null;
        boolean replace = false;

        if (param instanceof SQLCharExpr) {
            value = ((SQLCharExpr) param).getText();
            replace = true;
        }

        if (param instanceof SQLBooleanExpr) {
            value = ((SQLBooleanExpr) param).getBooleanValue();
            replace = true;
        }

        if (param instanceof SQLNumericLiteralExpr) {
            value = ((SQLNumericLiteralExpr) param).getNumber();
            replace = true;
        }

        if (param instanceof SQLHexExpr) {
            value = ((SQLHexExpr) param).toBytes();
            replace = true;
        }

        if (replace) {
            SQLObject parent = param.getParent();
            if (parent != null) {
                List<SQLObject> mergedList = null;
                if (parent instanceof SQLBinaryOpExpr) {
                    mergedList = ((SQLBinaryOpExpr) parent).getMergedList();
                }
                if (mergedList != null) {
                    List<Object> mergedListParams = new ArrayList<Object>(mergedList.size() + 1);
                    for (int i = 0; i < mergedList.size(); ++i) {
                        SQLObject item = mergedList.get(i);
                        if (item instanceof SQLBinaryOpExpr) {
                            SQLBinaryOpExpr binaryOpItem = (SQLBinaryOpExpr) item;
                            exportParameter(mergedListParams, binaryOpItem.getRight());
                        }
                    }
                    if (mergedListParams.size() > 0) {
                        mergedListParams.add(0, value);
                        value = mergedListParams;
                    }
                }
            }

            parameters.add(value);

            return new SQLVariantRefExpr("?");
        }

        return param;
    }

    public static void exportParameter(final List<Object> parameters, SQLBinaryOpExpr x) {
        if (x.getLeft() instanceof SQLLiteralExpr
                && x.getRight() instanceof SQLLiteralExpr
                && x.getOperator().isRelational()) {
            return;
        }

        {
            SQLExpr leftResult = ExportParameterVisitorUtils.exportParameter(parameters, x.getLeft());
            if (leftResult != x.getLeft()) {
                x.setLeft(leftResult);
            }
        }

        {
            SQLExpr rightResult = exportParameter(parameters, x.getRight());
            if (rightResult != x.getRight()) {
                x.setRight(rightResult);
            }
        }
    }

    public static void exportParameter(final List<Object> parameters, SQLBetweenExpr x) {
        {
            SQLExpr result = exportParameter(parameters, x.getBeginExpr());
            if (result != x.getBeginExpr()) {
                x.setBeginExpr(result);
            }
        }

        {
            SQLExpr result = exportParameter(parameters, x.getEndExpr());
            if (result != x.getBeginExpr()) {
                x.setEndExpr(result);
            }
        }

    }
}
