package battleship;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ServidorSocketStream {
	public static void main(String[] args) {
		Boolean sunk = false; // True cuando la coordenada introducida coincida con la de un barco
		boolean ready = false; // True cuando el jugador complete su tablero
		ArrayList<int[]> tablero = new ArrayList();
		int size = 8;
		int numBoats = 3;

		try {
			ServerSocket serverSocket = new ServerSocket();
			System.out.println("Realizando El Bind");
			InetSocketAddress addr = new InetSocketAddress("localhost", 5555);
			serverSocket.bind(addr);
			System.out.println("Aceptando Conexiones");
			Socket newSocket = serverSocket.accept();
			System.out.println("Conexi�n Recibida");
			InputStream is = newSocket.getInputStream();
			OutputStream os = newSocket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			ObjectInputStream ois = new ObjectInputStream(is);

			// Creando El Tablero
			System.out.println("---- Posiciona Tus Barcos! ---- ");
			System.out.println(
					"(El tablero es de " + size + "x" + size + " y tienes " + numBoats + " barcos de 1 espacio)");

			Scanner myObj = new Scanner(System.in);
			for (int i = 1; i <= numBoats; i++) {
				int[] pos = askForPos(size, myObj, i);
				// Comprobar si la posicion se repite
				for (int j = 0; j < tablero.size(); j++) {
					while (Arrays.toString(tablero.get(j)).equals(Arrays.toString(pos))) {
						System.out.println("Tus posiciones no pueden coincidir");
						pos = askForPos(size, myObj, i);
					}
				}
				tablero.add(pos);
			}
			System.out.println("Tu Tablero:");
			tablero.forEach((n) -> System.out.println(Arrays.toString(n)));
			ready = true;
			oos.writeBoolean(ready);
			oos.flush();
			ready = ois.readBoolean();

			// Empiezan a jugar
			while (!sunk) {
				sunk = clientTurn(sunk, tablero, oos, ois);
				System.out.println(sunk);
				if (!sunk) {
					sunk = serverTurn(oos, ois, myObj);
					System.out.println(sunk);
				}
			}

			System.out.println("Cerrando el nuevo socket");
			newSocket.close();
			System.out.println("Cerrando el socket servidor");
			serverSocket.close();
		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean serverTurn(ObjectOutputStream oos, ObjectInputStream ois, Scanner myObj) throws IOException {
		boolean sunk;
		String respuesta;
		System.out.println("---- Turno Del Servidor ----");
		System.out.println("Escribe Una Coordenada:");
		System.out.println("Coordenada X:");
		int posX = myObj.nextInt();
		System.out.println("Coordenada Y:");
		int posY = myObj.nextInt();
		int[] pos = { posX, posY };

		oos.writeObject(pos);
		oos.flush();

		System.out.println("Misil Enviado A La Posicion " + Arrays.toString(pos));
		sunk = ois.readBoolean();
		respuesta = sunk ? "Hundido!" : "Agua!";
		System.out.println(respuesta);
		return sunk;
	}

	private static Boolean clientTurn(boolean sunk, ArrayList<int[]> tablero, ObjectOutputStream oos,
			ObjectInputStream ois) throws IOException {
		String respuesta;
		System.out.println("---- Turno Del Cliente ---- ");

		try {
			int[] posJugador = (int[]) ois.readObject();
			System.out.println("Coordenadas Del Misil Del Cliente: " + Arrays.toString(posJugador));
			for (int i = 0; i < tablero.size(); i++) {
				if (Arrays.toString(tablero.get(i)).equals(Arrays.toString(posJugador))) {
					sunk = true;
				}
			}
			respuesta = sunk ? "Hundido!" : "Agua!";
			System.out.println(respuesta);
			oos.writeBoolean(sunk);
			oos.flush();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return sunk;
	}

	private static int[] askForPos(int size, Scanner myObj, int i) {
		System.out.println("-- Barco Numero " + i + " --");
		System.out.println("Coordenada X:");
		int posX = myObj.nextInt();
		while (posX < 1 || posX > size) {
			System.out.println("El numero debe estar entre 1-" + size);
			posX = myObj.nextInt();
		}
		System.out.println("Coordenada Y:");
		int posY = myObj.nextInt();
		while (posY < 1 || posY > size) {
			System.out.println("Introduce un numero entre 1-" + size);
			posY = myObj.nextInt();
		}
		int[] pos = { posX, posY };
		return pos;
	}
}