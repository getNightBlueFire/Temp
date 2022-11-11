import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        ObjectMapper mapper = new ObjectMapper();

        List<User> users = mapper.readValue(new File("C:\\Users\\Дмитрий\\Downloads\\users.json"), new TypeReference<>() {});
        System.out.println(users.size());


        List<Post> posts = mapper.readValue(new File("C:\\Users\\Дмитрий\\Downloads\\posts.json"), new TypeReference<>() {});
        System.out.println(posts.size());




        try (Connection connect = connect()){
            Statement statement = connect.createStatement();
            statement.execute("""
                Create table if not exists users(
            	id integer not null primary key,
            	username varchar not null
                )
            """);

            statement.execute("""
                Create table if not exists posts(
	id INTEGER NOT NULL,
	user_id INTEGER NOT NULL,
	title VARCHAR NOT NULL,
	body VARCHAR NOT NULL,
	CONSTRAINT POSTS_PK PRIMARY KEY (id),
	CONSTRAINT posts_FK FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);


            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO users(id, username) VALUES");
            for (User user : users) {
                String id = user.getId();
                String username = user.getUsername();
                sb.append("(").append(id).append(", ").append("'").append(username).append("'").append(")").append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            statement.execute(sb.toString());

            sb.setLength(0);
            sb.append("INSERT INTO posts(id, user_id, title, body) VALUES");
            for (Post post : posts) {
                sb.append("(")
                        .append(post.getId()).append(", ")
                        .append(post.getUserId()).append(", ")
                        .append("'").append(post.getTitle()).append("'").append(", ")
                        .append("'").append(post.getBody()).append("'")
                        .append(")").append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            statement.execute(sb.toString());
            statement.close();

            PreparedStatement preparedStatement = connect.prepareStatement("""
                        SELECT u.username FROM users u
                        JOIN posts p ON u.id = p.user_id
                        WHERE p.id = ?
                        LIMIT 1
                    """);

            int idPostFromUrl = new Scanner(System.in).nextInt();

            preparedStatement.setInt(1, idPostFromUrl);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                String title = resultSet.getString(1);
                System.out.println(title);
            }

            preparedStatement.close();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static Connection connect() throws SQLException {
        Connection connection;
        String url = "jdbc:sqlite:D:\\test";
        connection = DriverManager.getConnection(url);
        return connection;
    }


}