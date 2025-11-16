package fiap.tech.challenge.online.course.report.serverless.dao;

import fiap.tech.challenge.online.course.report.serverless.config.DataSourceConfig;
import fiap.tech.challenge.online.course.report.serverless.payload.AssessmentType;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportResponse;

import java.sql.*;
import java.util.*;

public class FTCOnlineCourseReportServerlessDAO {

    private final Connection connection;

    public FTCOnlineCourseReportServerlessDAO() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(dataSourceConfig.getJdbcUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
            if (!connection.isValid(0)) {
                throw new SQLException("Não foi possível estabelecer uma conexão com o banco de dados. URL de conexão: " + connection.getMetaData().getURL());
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public FeedbackReportResponse getFeedbackReportByHashId(FeedbackReportRequest feedbackReportRequest) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT tf.urgent as urgent, tf.description as description, tf.comment as comment, tadmin.name as administrator_name, tadmin.email as administrator_email, tt.name as teacher_name, tt.email as teacher_email, ts.name as student_name, ts.email as student_email, ta.name as assessment_name, ta.type as assessment_type, ta.score as assessment_score, tf.created_in as created_in FROM public.t_feedback tf " +
                    "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                    "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                    "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                    "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                    "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                    "WHERE tf.hash_id = ? AND tf.urgent = TRUE;");
            preparedStatement.setString(1, feedbackReportRequest.hashIdFeedback());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new NoSuchElementException("Nenhum feedback urgente foi encontrado com o hash id informado para o envio de e-mail.");
            } else {
                return new FeedbackReportResponse(
                        resultSet.getBoolean("urgent"),
                        resultSet.getString("description"),
                        resultSet.getString("comment"),
                        resultSet.getString("administrator_name"),
                        resultSet.getString("administrator_email"),
                        resultSet.getString("teacher_name"),
                        resultSet.getString("teacher_email"),
                        resultSet.getString("student_name"),
                        resultSet.getString("student_email"),
                        resultSet.getString("assessment_name"),
                        AssessmentType.valueOf(resultSet.getString("assessment_type")),
                        resultSet.getDouble("assessment_score"),
                        resultSet.getTimestamp("created_in", Calendar.getInstance(TimeZone.getTimeZone("GMT-3"))).toString()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerFeedbackReport(FeedbackReportRequest feedbackReportRequest, FeedbackReportResponse feedbackReportResponse) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO t_urgent_email_notification(id, created_by, has_been_sent, fk_feedback) " +
                            "VALUES (nextval('sq_weekly_email_notification'), ?, true, (SELECT tf.id FROM t_feedback tf WHERE tf.hash_id = ?))");
            preparedStatement.setString(1, feedbackReportResponse.administratorEmail());
            preparedStatement.setString(2, feedbackReportRequest.hashIdFeedback());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected < 1) {
                throw new SQLException("Ocorreu um problema ao cadastrar um envio de urgência de e-mail. Tente novamente mais tarde.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}