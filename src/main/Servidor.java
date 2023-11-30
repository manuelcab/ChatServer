package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    
    private ServerSocket servidor;
    private List<Administrador> clientes = new ArrayList<>();
    private Connection conexionBD;
    
    public Servidor(int puerto, String ipAddress, Connection conexionBD) {
        
        try {
            
            servidor = new ServerSocket(puerto, 0, InetAddress.getByName(ipAddress));
            System.out.println("Servidor iniciado en " + ipAddress + " puerto " + puerto);
            this.conexionBD = conexionBD;
            
        } catch (IOException e) {
        }
    }
    
    public void start() {
        while (true) {
            try {
                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado: " + cliente);

                Administrador administrador = new Administrador(this, cliente, conexionBD);
                clientes.add(administrador);
                new Thread(administrador).start();
            } catch (IOException e) {
            }
        }
    }

    public void reenviarMensaje(String mensaje, Administrador remitente) {
        for (Administrador cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarMensaje(remitente.obtenerUsuario() + ": " + mensaje);
            }
        }
        
    }

    public static void main(String[] args) throws SQLException {
        
        int PUERTO = 8080;
        String HOST = "localhost";
        
        Connection conexionBD = ConexionBD.obtenerConexionBD();

        Servidor servidor = new Servidor(PUERTO, HOST, conexionBD);
        servidor.start();
    }
}