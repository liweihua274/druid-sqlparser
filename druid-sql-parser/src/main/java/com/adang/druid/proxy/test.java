package com.adang.druid.proxy;


import com.adang.druid.proxy.sql.ast.SQLStatement;
import com.adang.druid.proxy.sql.dialect.mysql.parser.MySqlStatementParser;
import com.adang.druid.proxy.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.adang.druid.proxy.sql.parser.SQLStatementParser;

public class test {
  public static void main(String[] args) {

   /* String sql = "SELECT UUID();";

    // parser得到AST
    SQLStatementParser parser = new MySqlStatementParser(sql);
    List<SQLStatement> stmtList = parser.parseStatementList(); //

    // 将AST通过visitor输出
    StringBuilder out = new StringBuilder();
    MySqlOutputVisitor visitor = new MySqlOutputVisitor(out);

    for (SQLStatement stmt : stmtList) {
      stmt.accept(visitor);
      out.append(";");
    }

    System.out.println(out.toString());*/

    String sql = "CREATE TABLE `iaaction` (\n" +
            "  `uuid` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" +
            "  `actionKey` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,\n" +
            "  `actionCaption` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,\n" +
            "  PRIMARY KEY (`uuid`),\n" +
            "  KEY `idx_IAAction_1` (`actionKey`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;\n" +
            "\n";

    // 新建 MySQL Parser
    SQLStatementParser parser = new MySqlStatementParser(sql);

    // 使用Parser解析生成AST，这里SQLStatement就是AST
    SQLStatement statement = parser.parseStatement();

    // 使用visitor来访问AST
    MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
    statement.accept(visitor);

    // 从visitor中拿出你所关注的信息
    System.out.println(visitor.getColumns());
  }

}
