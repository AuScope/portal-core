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
 * Will limit the number of states stored according to a constructor parameter
 */
public class StateService {
    private final Log logger = LogFactory.getLog(getClass());
    private Connection con;
    private static String DB_NAME = "state.db";
    private int limitRows; // Row limit in state table

    /**
     * Constructor - creates an SQLite db in memory
     * 
     * @param maxStates limit to the number of states stored in the database
     *                  will set a limit of 1000 if incorrect value given
     */
    public StateService(int maxStates) {
        startDb("jdbc:sqlite::memory:");
        if (maxStates > 1) {
            this.limitRows = maxStates - 1;
        } else {
            this.limitRows = 999;
        }
          
    }

    /**
     * Constructor - creates an SQLite db in local filesystem
     *
     * @param localDir local filesystem directory to save SQLite db
     * @param maxStates limit to the number of states stored in the database
     *                  will set a limit of 1000 if incorrect value given
     */
    public StateService(String localDir, int maxStates) {
        startDb("jdbc:sqlite:" + localDir + DB_NAME);
        if (maxStates > 1) {
            this.limitRows = maxStates - 1;
        } else {
            this.limitRows = 999;
        }
    }

    /**
     * Open database and create a table if necessary
     * @param url SQLite JDBC connection string 
     */
    private void startDb(String url) {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS STATES (\n"
        + " id text PRIMARY KEY,\n"
        + " state text NOT NULL,\n"
        + " create_date date NOT NULL\n"
        + ");";
        try {
            this.con = DriverManager.getConnection(url);
            Statement stmt = this.con.createStatement();
            stmt.execute(sql);
            logger.info("Opened state service database: " + url);
        } catch (SQLException ex) {
            logger.error("Cannot open state service database: ", ex);
        }
    }

    /**
     * Save the state in the database return an id string
     *
     * @param id an id to associate with state string
     * @param state state string
     * @return boolean, true if successful, if association already exists returns true
     */
    public boolean save(String id, String state) {
        String testState = fetch(id);
        // If already saved return true
        if (testState.equals(state)) {
            return true;
        }
        // If already saved as another value or nulls return false
        if (!testState.equals("") || id == null || state == null) {
            return false;
        }
        // Remove all but the most recent 'maxRows' records
        try {
            Statement statement = this.con.createStatement();
            String pruneSQL = "DELETE FROM STATES WHERE ROWID NOT IN (SELECT ROWID FROM STATES ORDER BY CREATE_DATE DESC LIMIT " + 
                               Integer.toString(this.limitRows) + ");";
            statement.executeUpdate(pruneSQL);
        } catch (SQLException ex) {
            logger.error("Cannot prune database: id=" + id + " & state=" + state + " to state db: ", ex);
            return false;
        }
        // Insert new record into db
        try {
            PreparedStatement prepStmt = this.con.prepareStatement("INSERT INTO STATES (id, state, create_date) VALUES(?,?,?)");
            prepStmt.setString(1, id);
            prepStmt.setString(2, state);
            Date currentDate = new Date();
            prepStmt.setTimestamp(3, new Timestamp(currentDate.getTime()));
            prepStmt.executeUpdate();
            prepStmt.close();
        } catch (SQLException ex) {
            logger.error("Cannot save id=" + id + " & state=" + state + " to state db: ", ex);
            return false;
        }
        return true;
    }

    /**
     * Fetch the state from the database given an id string
     *
     * @param id id string
     * @return state string, or an empty string upon failure
     */
    public String fetch(String id) {
        String result="";
        if (id != null) {
            try {
                var prepStmt = this.con.prepareStatement("SELECT STATE FROM STATES WHERE ID=?");
                prepStmt.setString(1, id);
                ResultSet rs = prepStmt.executeQuery();
                if (rs.next()) {
                    result = rs.getString("STATE");
                }
            } catch (SQLException ex) {
                logger.error("Cannot fetch id=" + id + " from state db: ", ex);
            }
        }
        return result;
    }

}
