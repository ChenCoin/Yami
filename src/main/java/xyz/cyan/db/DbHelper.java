package xyz.cyan.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class DbHelper {

    public void run() {

        try {
            System.out.println("Load the embedded driver");

            Properties props = new Properties();
            props.put("user", "root");
            props.put("password", "123456");
            //new EmbeddedDriver();
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

            Connection conn = DriverManager.getConnection("jdbc:derby:yamiDB;create=true", props);
            System.out.println("create and connect to helloDB");
            conn.setAutoCommit(false);

            Statement s = conn.createStatement();
            s.execute("create table hellotable(name varchar(40), score int)");

            System.out.println("Created table hellotable");
            s.execute("insert into hellotable values('Ruth Cao', 86)");
            s.execute("insert into hellotable values ('Flora Shi', 92)");

            // list the two records
            ResultSet rs = s.executeQuery("SELECT name, score FROM hellotable ORDER BY score");
            System.out.println("namettscore");
            while (rs.next()) {
                StringBuilder builder = new StringBuilder(rs.getString(1));
                builder.append("t");
                builder.append(rs.getInt(2));
                System.out.println(builder.toString());
            }

            // delete the table
            s.execute("drop table hellotable");
            System.out.println("Dropped table hellotable");

            rs.close();
            s.close();
            System.out.println("Closed result set and statement");
            conn.commit();
            conn.close();
            System.out.println("Committed transaction and closed connection");
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

}
