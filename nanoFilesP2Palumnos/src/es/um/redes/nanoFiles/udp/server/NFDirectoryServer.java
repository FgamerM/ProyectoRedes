package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */




	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina
		 * local,
		 */
		
		socket=new DatagramSocket(DIRECTORY_PORT);
		
		/*
		 * TODO: (Boletín UDP) Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */

		nicks = new HashMap<String, Integer>();
		sessionKeys = new HashMap<Integer, String>();

		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		byte[] receptionBuffer = null;  
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		/*
		 * TODO: (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */

		receptionBuffer = new byte[128];
		DatagramPacket packetFromClient = new DatagramPacket(receptionBuffer, receptionBuffer.length);


		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			// TODO: (Boletín UDP) Recibimos a través del socket un datagrama
			
			socket.receive(packetFromClient);

			// TODO: (Boletín UDP) Establecemos dataLength con longitud del datagrama
			// recibido
			
			dataLength=packetFromClient.getLength();

			// TODO: (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del datagrama recibido

			clientAddr=(InetSocketAddress) packetFromClient.getSocketAddress();


			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				String messageFromClient = null;
				/*
				 * TODO: (Boletín UDP) Construir una cadena a partir de los datos recibidos en
				 * el buffer de recepción
				 */
				messageFromClient = new String(receptionBuffer, 0, packetFromClient.getLength());

				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					/*
					 * TODO: (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
					 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
					 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
					 * y no se envía ninguna respuesta.
					 */
					if(messageFromClient.equals("login")) {
						byte[] mensaje = "loginok".getBytes();
						DatagramPacket packetToServer = new DatagramPacket(mensaje, mensaje.length, clientAddr);
						try {
							socket.send(packetToServer);
							} catch(IOException e) {
								e.printStackTrace();
							}
					} else {
						System.out.println("Se esperaba la cadena login");
					}
					

				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
					
					
				//ESTO ES LO DE ANTES DE LOS MENSAJES BUENOS	
//				//	System.out.println("quiero mandarte la respuesta pero el mensaje es "+messageFromClient);
//				//	String[] cadenaDividida = messageFromClient.split("&");
//					
//					String mensajeClienteStr = new String(messageFromClient);
//                    if(mensajeClienteStr.startsWith("login")) {
//                        String nickname = mensajeClienteStr.substring(6);
//                        String strResponse = null;
//                        if(this.nicks.containsKey(nickname)) {
//                            strResponse = "login_failed:-1";
//                        }
//                        else {
//                            int num;
//                            do {
//                                num = random.nextInt(1000);
//                            } while (this.nicks.containsValue(num));
//
//                            this.nicks.put(nickname, num);
//                            strResponse = "loginok&"+num;
//                            System.out.println("Login aceptado");
//                        }
//
//
//                        DatagramPacket packetToSend = new DatagramPacket(strResponse.getBytes(), strResponse.length(), clientAddr);
//                        this.socket.send(packetToSend);
//                        System.out.println();
//                    }
//                    

					/*
					 * TODO: Construir String a partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).
					 */
					System.out.println("La cadena recibida es "+messageFromClient);//Supongo que depuración
					DirMessage mensaje = DirMessage.fromString(messageFromClient);
					
					/*
					 * TODO: Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					
					DirMessage MessageToSend=this.buildResponseFromRequest(mensaje, clientAddr);
					
					/*
					 * TODO: Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */
					
					String MessageToSendString = MessageToSend.toString();//fino, Aquí peta
					byte[] MessageToSendBytes=MessageToSendString.getBytes();
					DatagramPacket packetToServer = new DatagramPacket(MessageToSendBytes, MessageToSendBytes.length, clientAddr);
					try {
						socket.send(packetToServer);
						} catch(IOException e) {
							e.printStackTrace();
						}
						
				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * TODO: Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();

		DirMessage response = null;




		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();
			int sessionkey=-1;
			if(this.nicks.containsKey(username)) {
				response=new DirMessage(DirMessageOps.OPERATION_INVALIDNICKNAME);
			}
			else {
				do {
					sessionkey = random.nextInt(10000);
				}while(this.sessionKeys.containsKey(sessionkey));
				
				this.nicks.put(username, sessionkey);
				this.sessionKeys.put(sessionkey, username);
				response = new DirMessage(DirMessageOps.OPERATION_LOGINOK);
				response.setNickname(username);
				response.setSessionkey(sessionkey);
			}
			break;

			/*
			 * TODO: Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
			 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
			 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(10000)
			 * para generar la session key
			 */
			/*
			 * TODO: Construimos un mensaje de respuesta que indique el éxito/fracaso del
			 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
			 * resultado del método.
			 */
			/*
			 * TODO: Imprimimos por pantalla el resultado de procesar la petición recibida
			 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
			 * servidor
			 */



			
		}case DirMessageOps.OPERATION_LOGOUT : {
			int sessionkey = msg.getSessionkey();
			
			if(this.sessionKeys.containsKey(sessionkey)) { 
				response = new DirMessage(DirMessageOps.OPERATION_LOGOUTOK);
				this.nicks.remove(this.sessionKeys.get(sessionkey));
				this.sessionKeys.remove(sessionkey);
				
			}
			else {			
				response = new DirMessage(DirMessageOps.OPERATION_LOGOUTFAIL);
				System.err.println("Sesion key no asignada: "+sessionkey);
							 
			}
			break;
			
		}case DirMessageOps.OPERATION_GETUSERLIST : {
			
			if(this.sessionKeys.containsKey(msg.getSessionkey())) {
				response = new DirMessage(DirMessageOps.OPERATION_USERLISTOK);
				String[] userList = this.nicks.keySet().toArray(new String[0]);
				response.setUserlist(userList);
				
			}
			else {
				response = new DirMessage(DirMessageOps.OPERATION_USERLISTFAIL);
				
			}
			
			break;
			
		}



		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"");
			System.out.println("Answer: ");
			System.out.println(response);
		}
		return response;

	}
}
