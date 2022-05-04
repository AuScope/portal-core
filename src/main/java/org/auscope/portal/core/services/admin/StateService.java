package org.auscope.portal.core.services.admin;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Date;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Service class providing a permanent storage for application state, e.g. user interface state
 */
public class StateService {
    private final Log logger = LogFactory.getLog(getClass());
    private Connection con;
    private static String DB_NAME = "state.db";

    public StateService(String localDir) {
         // SQLite connection string
         String url = "jdbc:sqlite:" + localDir + DB_NAME;

         // SQL statement for creating a new table
         String sql = "CREATE TABLE IF NOT EXISTS STATES (\n"
                 + " id text PRIMARY KEY,\n"
                 + " state text NOT NULL,\n"
                 + " create_date date NOT NULL\n"
                 + ");";
         try {
            con = DriverManager.getConnection(url);
            Statement stmt = con.createStatement();
            stmt.execute(sql);
            logger.info("Opened state service database: " + url);
         } catch (SQLException ex) {
            logger.error("Cannot open state service database: ", ex);
         }
    }

    /**
     * Save the state in the database return an id string
     *
     * @param state state string
     * @return inserted id as a string
     */
    public boolean save(String id, String state) {
        try {
            PreparedStatement prepStmt = con.prepareStatement("INSERT INTO STATES (id, state, create_date) VALUES(?,?,?)");
            prepStmt.setString(1, id);
            prepStmt.setString(2, state);
            Date currentDate = new Date();
            prepStmt.setTimestamp(3, new Timestamp(currentDate.getTime()));
            prepStmt.executeUpdate();
            prepStmt.close();
        } catch (SQLException ex) {
            logger.error("Cannot save to state db: ", ex);
            return false;
        }
        return true;
    }

    /**
     * Fetch the state from the database given an id string
     *
     * @param id id string
     * @return state string
     */
    public String fetch(String id) {
        String result="";
        try {
            var prepStmt = con.prepareStatement("SELECT STATE FROM STATES WHERE ID=?");
            prepStmt.setString(1, id);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.next()) {
                result = rs.getString("STATE");
            }
        } catch (SQLException ex) {
            logger.error("Cannot fetch from state db: ", ex);
        }
        return result;
    }

}
