package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                cfg.getProperty("url"),
                cfg.getProperty("username"),
                cfg.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement(
            "insert into post(name, text, link, created) values(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                post.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("select * from post")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(getPost(resultSet));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                post = getPost(resultSet);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return post;
    }

    private Post getPost(ResultSet rs) throws SQLException {
        return new Post(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("link"),
            rs.getString("text"),
            rs.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        Post post1 = new Post("testName1", "testLink1", "testText1", LocalDateTime.now());
        Post post2 = new Post("testName2", "testLink2", "testText2", LocalDateTime.now());
        try (PsqlStore psqlStore = new PsqlStore(config)) {
            psqlStore.save(post1);
            psqlStore.save(post2);
            System.out.println(psqlStore.getAll());
            System.out.println(psqlStore.findById(post1.getId()));
            System.out.println(psqlStore.findById(post2.getId()));
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}
