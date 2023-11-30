package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    public static Connection obtenerConexionBD() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/chat_grupo_db";
        String usuario = "root";
        String contraseña = "";

        return DriverManager.getConnection(url, usuario, contraseña);
    }
}
