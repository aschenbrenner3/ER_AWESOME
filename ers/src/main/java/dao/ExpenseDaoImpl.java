package dao;

import model.Expense;
import model.ExpenseStatus;
import model.ExpenseType;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by qzh225 on 4/17/17.
 */
public class ExpenseDaoImpl implements ExpenseDao{

    @Override
    public ArrayList<Expense> getAllExpenses(){

        ArrayList<Expense> expenses = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        UserDaoImpl dao = new UserDaoImpl();

        try {
            connection = DaoUtilities.getConnection();
            String sql =  "SELECT a.*, b.u_id as author_id, b.u_username as authorname,";
            sql = sql + "  c.u_id as resolver_id, c.u_username as resolvername, d.rs_id, d.rs_status, e.rt_id, e.rt_type";
            sql = sql + "  from erawesome.ers_reimbursements a";
            sql = sql + "  join erawesome.ers_users b on b.u_id = a.u_id_author";
            sql = sql + "  join erawesome.ers_users c on c.u_id = a.u_id_resolver";
            sql = sql + "  join erawesome.ers_reimbursement_status d on d.rs_id = a.rs_status";
            sql = sql + "  join erawesome.ers_reimbursement_type e on e.rt_id = a.rt_type";

            stmt = connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Expense exp = new Expense();

                exp.setR_id(rs.getInt("r_id"));
                exp.setR_amount(rs.getDouble("r_amount"));
                exp.setR_description(rs.getString("r_description"));
                exp.setR_submitted(rs.getTimestamp("r_submitted"));
                exp.setR_resolved(rs.getTimestamp("r_resolved"));

                //Add Author user object
                User author = dao.getUser(rs.getString("authorname"));
                exp.setU_author(author);

                //Add Resolver user object
                User resolver = dao.getUser(rs.getString("resolvername"));
                exp.setU_resolver(resolver);

                //Add Expense Type Object
                ExpenseType etype = new ExpenseType();
                etype.setRt_id(rs.getInt("rt_id"));
                etype.setRt_type(rs.getString("rt_type"));
                exp.setR_type(etype);

                //Add Expense Status Object
                ExpenseStatus estat = new ExpenseStatus();
                estat.setRs_id(rs.getInt("rs_id"));
                estat.setRs_status(rs.getString("rs_status"));
                exp.setR_status(estat);

                expenses.add(exp);
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return expenses;
    }

    @Override
    public void AddReimbursement (Expense reimb){
        Connection connection = null;
        PreparedStatement stmt = null;
        int success = 0;

        try{
            connection = DaoUtilities.getConnection();
            String sql =  "INSERT Into erawesome.ers_reimbursements ( r_amount, r_description, r_submitted , ";
            sql = sql + "  r_resolved, r_id_author , u_id_resolver, rt_type, rt_status) ";
            sql = sql + "  VALUES (?,?,?,?,?,?,?,? ) ";

            stmt = connection.prepareStatement(sql);


            stmt.setDouble(1, reimb.getR_amount());
            stmt.setString(2, reimb.getR_description());
            stmt.setTimestamp(3, reimb.getR_submitted());
            stmt.setTimestamp(4, reimb.getR_resolved());
            stmt.setInt(5, reimb.getU_author().getU_id());
            stmt.setInt(6, reimb.getU_resolver().getU_id());
            stmt.setInt(7, reimb.getR_type().getRt_id());
            stmt.setInt(8, reimb.getR_status().getRs_id());

            success = stmt.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
            //todo log insert failure
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (success != 0) {
            //TODO log insert success
        }
    }

    @Override
    public void UpdateReimbursement(Expense reimb) {
        Connection connection = null;
        PreparedStatement stmt = null;
        int success = 0;

        try{
            connection = DaoUtilities.getConnection();
            String sql =  "UPDATE erawesome.ers_reimbursements set r_amount = ?, r_description = ?, r_submitted = ?, ";
            sql = sql + "r_resolved = ?, r_id_author = ? , u_id_resolver = ?, rt_type = ?, rt_status = ? WHERE r_id = ?";
            stmt = connection.prepareStatement(sql);


            stmt.setDouble(1, reimb.getR_amount());
            stmt.setString(2, reimb.getR_description());
            stmt.setTimestamp(3, reimb.getR_submitted());
            stmt.setTimestamp(4, reimb.getR_resolved());
            stmt.setInt(5, reimb.getU_author().getU_id());
            stmt.setInt(6, reimb.getU_resolver().getU_id());
            stmt.setInt(7, reimb.getR_type().getRt_id());
            stmt.setInt(8, reimb.getR_status().getRs_id());
            stmt.setInt(9, reimb.getR_id());

            success = stmt.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
            //todo log update failure
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (success != 0) {
            //TODO log update success
        }
    }

    public ArrayList<ExpenseType> getExpenseTypeList() {

        ArrayList<ExpenseType> exptypes = new ArrayList<>();
        PreparedStatement stmt = null;

        try (Connection connection = DaoUtilities.getConnection()) {

            String sql =  "SELECT rt_id, rt_type from erawesome.ers_reimbursement_type ";

            stmt = connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                ExpenseType etype = new ExpenseType();

                etype.setRt_id(rs.getInt("rt_id"));
                etype.setRt_type(rs.getString("rt_type"));

                exptypes.add(etype);
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
            //todo LOG Failure to retrieve ExpenseType list
        }

        return exptypes;

    }

    public ArrayList<ExpenseStatus> getExpenseStatusList() {

        ArrayList<ExpenseStatus> expstats = new ArrayList<>();
        PreparedStatement stmt = null;

        try (Connection connection = DaoUtilities.getConnection()) {

            String sql =  "SELECT rs_id, rs_status from erawesome.ers_reimbursement_status ";

            stmt = connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                ExpenseStatus estat = new ExpenseStatus();

                estat.setRs_id(rs.getInt("rs_id"));
                estat.setRs_status(rs.getString("rs_status"));

                expstats.add(estat);
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
            //todo LOG Failure to retrieve ExpenseStatus list
        }

        return expstats;

    }


}
