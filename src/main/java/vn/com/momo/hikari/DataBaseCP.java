package vn.com.momo.hikari;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import vn.com.momo.app.AppConfig;
import vn.com.momo.app.AppUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;
import java.util.function.Consumer;
import java.util.function.Function;

@Log4j2
public class DataBaseCP {

    private static DataBaseCP instance = new DataBaseCP();
    private HikariDataSource hds;

    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;

    public DataBaseCP() {
        try {
            log.info("config " + AppConfig.getInstance().getOracleClient());
            hds = new HikariDataSource(new HikariConfig(AppConfig.getInstance().getOracleClient()));
            log.info("hds: " + hds);
        } catch (Exception e) {
            log.error(AppUtils.getFullStackTrace(e));
        }
    }
    public Connection getConnection() {
        try {
            if (connection == null) {
                connection = hds.getConnection();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public void insert(String sql) {

        try {
            connection = hds.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sql);

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            log.error(AppUtils.getFullStackTrace(e));
        } finally {
            close();
        }
    }

    public void query(String sql, Consumer<ResultSet> consumer) {

        try {
            connection = hds.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                consumer.accept(resultSet);
            }
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            log.error(AppUtils.getFullStackTrace(e));
        } finally {
            close();
        }
    }
    /*public void query(String sql) {

        try {
            connection = hds.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                consumer.accept(resultSet);
            }
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            log.error(AppUtils.getFullStackTrace(e));
        } finally {
            close();
        }
    }*/

    public Integer query(String sql, Function<ResultSet, Integer> function) {

        try {
            connection = hds.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                return function.apply(resultSet);
            }
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            log.error(AppUtils.getFullStackTrace(e));
        } finally {
            close();
        }

        return 0;
    }

    public static DataBaseCP getInstance() {
        return instance;
    }

    private void close() {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error(AppUtils.getFullStackTrace(e));
        } catch (Exception e) {
            log.error(AppUtils.getFullStackTrace(e));
        }
    }
}
