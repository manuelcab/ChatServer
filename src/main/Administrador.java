package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Administrador implements Runnable {

    private Servidor servidor;
    private Socket clientSocket;
    private BufferedReader leer;
    private BufferedWriter escribir;
    private Connection conexionBD;
    private String usuario;

    public Administrador(Servidor servidor, Socket clienteSocket, Connection conexionBD) {
        this.servidor = servidor;
        this.clientSocket = clienteSocket;
        this.conexionBD = conexionBD;
        
        try {
            leer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            escribir = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
        }  
    }

    @Override
    public void run() {
        
        try {

            String operacion = leer.readLine();

            String[] partes = operacion.split(":");
            
            if (partes.length == 3 && "LOGIN".equals(partes[0])) {
                this.usuario = partes[1];
                String contrasena = partes[2];

                if (validarUsuarioEnBD(usuario, contrasena)) {
                    
                    escribir.write("SESION_INICIADA");
                    escribir.newLine();
                    escribir.flush();
                    System.out.println("SESION_INICIADA");
                    
                } else {
                    
                    escribir.write("FALLO_SESION");
                    escribir.newLine();
                    escribir.flush();
                    System.out.println("FALLO_SESION");
                    
                }
            } else if(partes.length == 3 && "REGISTER".equals(partes[0])) {

                this.usuario = partes[1];
                String contrasena = partes[2];

                if (registrarUsuarioEnBD(usuario, contrasena)) {
                    
                    escribir.write("REGSITRO_EXITOSO");
                    escribir.newLine();
                    escribir.flush();
                    System.out.println("REGSITRO_EXITOSO");
                    
                } else {
                    
                    escribir.write("FALLO_REGISTRO");
                    escribir.newLine();
                    escribir.flush();
                    System.out.println("FALLO_REGISTRO");
                    
                }
            }
            

            while (true) {
                String mensaje = leer.readLine();
                if (mensaje == null) {
                    break;
                }
                System.out.println(usuario + ": " + mensaje);
                servidor.reenviarMensaje(usuario + ": " + mensaje, this);
            }

        } catch (IOException e) {
        }
    }
    
    public void enviarMensaje(String mensaje) {
        try {
            escribir.write(mensaje + "\n");
            escribir.flush();
            
            System.out.println(mensaje);
        } catch (IOException e) {
        }
    }
    
    public String obtenerUsuario() {
        return usuario;
    }

    private boolean validarUsuarioEnBD(String usuario, String contrasena) {
        try {
            String consulta = "SELECT * FROM usuarios WHERE USER=? AND PASSWORD=? AND ACTIVO=?";
            try (PreparedStatement statement = conexionBD.prepareStatement(consulta)) {
                statement.setString(1, usuario);
                statement.setString(2, contrasena);
                statement.setBoolean(3, false);  // Verifica si el usuario está activo

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        
                        // Marcar al usuario como activo en la base de datos
                        activarUsuario(usuario);
                        return true;
                        
                    } else {
                        // Usuario no válido o activo
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private boolean registrarUsuarioEnBD(String usuario, String contrasena) {

        try {
            
            // Verificar si ya existe un usuario con el mismo nombre
            if (existeUsuarioEnBD(usuario)) {
                System.out.println("Ya existe un usuario con el nombre: " + usuario);
                return false;
            }
            
            String insercion = "INSERT INTO usuarios (USER, PASSWORD) VALUES (?, ?)";
            
            try (PreparedStatement statement = conexionBD.prepareStatement(insercion)) {
                statement.setString(1, usuario);
                statement.setString(2, contrasena);
                statement.executeUpdate();
                return true;
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean existeUsuarioEnBD(String usuario) {
        try {
            String consulta = "SELECT * FROM usuarios WHERE USER=?";
            try (PreparedStatement statement = conexionBD.prepareStatement(consulta)) {
                statement.setString(1, usuario);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next(); // Devuelve true si hay al menos un resultado, es decir, el usuario ya existe
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void activarUsuario(String usuario) {
        try {
            String actualizacion = "UPDATE usuarios SET ACTIVO=TRUE WHERE USER=?";
            try (PreparedStatement statement = conexionBD.prepareStatement(actualizacion)) {
                statement.setString(1, usuario);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void desactivarUsuario(String usuario) {
        try {
            String actualizacion = "UPDATE usuarios SET ACTIVO=FALSE WHERE USER=?";
            try (PreparedStatement statement = conexionBD.prepareStatement(actualizacion)) {
                statement.setString(1, usuario);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}