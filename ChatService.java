package worshop.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v1.model.CreateWorkspaceOptions;
import com.ibm.watson.assistant.v1.model.GetWorkspaceOptions;
import com.ibm.watson.assistant.v1.model.Workspace;
import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant.v1.model.RuntimeEntity;
import com.ibm.watson.developer_cloud.assistant.v1.model.RuntimeIntent;
import com.ibm.watson.developer_cloud.service.security.IamOptions;

@Path("/chatservice")
public class ChatService {
	
  private String urlDB;
  private String userDB;
  private String passwordDB;
  private static String apiKey="9an1UGArJqLwh9B1FyETFI7zKF8rKc32LhsS187w_rmU"; 
  private static String assistantURL="https://api.us-east.assistant.watson.cloud.ibm.com/instances/e3fcfef3-02fb-4b36-9047-5cb784477148"; 
  private static String workspaceId = "a343d669-e093-4340-80bf-822ade0f35e7";
  private static Context context = new Context();
  private static InputData input;
  private static MessageOptions options;
  private static Assistant service;
  private static MessageResponse response;

  public ChatService(){
    try {
	} catch (Exception e) {
		e.printStackTrace();
		}
	}
  
  @GET
  @Produces("application/json")
  public Response getResponse(@QueryParam("conversationMsg") String conversationMsg, @QueryParam("conversationCtx") String conversationCtx) {
    IamOptions iAmOptions = new IamOptions.Builder()
      .apiKey(apiKey)
      .build();

    Assistant service = new Assistant("2018-09-20", iAmOptions);
    service.setEndPoint(assistantURL);
    
    // Initialize with empty value to start the conversation.
    JSONObject ctxJsonObj = new JSONObject(conversationCtx);
    Context context = new Context();
    context.putAll(ctxJsonObj.toMap());
			
    InputData input = new InputData.Builder(conversationMsg).build();
    MessageOptions options = new MessageOptions.Builder(workspaceId).input(input).context(context).build();
    	
    MessageResponse assistantResponse = service.message(options).execute();
    System.out.println(assistantResponse);
			
    //METER INFO EN WATSON
	String saludo=obtenerSaludo();
	context.put("saludo", saludo);
	context.get("pruebaGuardado2");
	System.out.print("pruebaGuardado2");
 
  	List<RuntimeEntity> entidades= assistantResponse.getEntities();
    input = new InputData.Builder(conversationMsg).build();
	options = new MessageOptions.Builder(workspaceId).input(input).context(context).build();
	assistantResponse = service.message(options).execute();

	// Print the output from dialog, if any.
	List<String> assistantResponseList = assistantResponse.getOutput().getText();
	JSONObject object = new JSONObject();	
    String assistantResponseText = "";
      for (String tmpMsg : assistantResponseList)  
	    //assistantResponseText = tmpMsg;	
    	assistantResponseText = assistantResponseText + System.lineSeparator() + tmpMsg;
      	object.put("response", assistantResponseText);
		object.put("context", assistantResponse.getContext());
		String respuesta=(String)assistantResponseText;
		System.out.println(respuesta);
		
		return Response.status(Status.OK).entity(object.toString()).build();	
     }
		
  
  public static void main(String[] args) {	
  // Suppress log messages in stdout.
  LogManager.getLogManager().reset();
  // Set up Assistant service.
  IamOptions iAmOptions2 = new IamOptions.Builder().apiKey(apiKey).build();
  service = new Assistant("2018-09-20", iAmOptions2);
  service.setEndPoint(assistantURL);
  // Initialize with empty value to start the conversation.
  options = new MessageOptions.Builder(workspaceId).build();
  try {
  // Initialize with empty value to start the conversation.
    String currentAction = "";
    String currentIntent = "";
  // Main input/output loop
  do {
      // Send message to Assistant service.
      enviarMensaje();
	  // Print the output from dialog, if any.
	  cargarRespuesta();
	  recibirTextoUsuario();				 
     // If we're not done, prompt for next round of input.
	 System.out.print(">> ");
	 BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	 String inputText = in.readLine();
	 input = new InputData.Builder(inputText).build();					
	//Metemos informaciOn
	 String saludo=obtenerSaludo();
	 context.put("saludo", saludo);				
	 } while (!currentAction.equals("quit"));
	 } catch (SecurityException e) {
	   e.printStackTrace();
	 } catch (RuntimeException e) {
	   e.printStackTrace();
	 } catch (IOException e) {
	   e.printStackTrace();
	 }
	}
		
  private static void enviarMensaje() {
    options = new MessageOptions.Builder(workspaceId).input(input).context(context).build();
    response = service.message(options).execute();
	//imprime todo
	//System.out.println(response+"ESTO*******ENVIARmensaje");
	}
    
  private static InputData recibirTextoUsuario() {
		context.get(input);
		System.out.println(input);
		return input;
	}
  
  private static String recibirPreguntasFormularioRegistro() {
    String preguntaUnoFormularioRegistroUsuario  = (String) context.get("formularioRegistroNombre");
    System.out.print(preguntaUnoFormularioRegistroUsuario);
    return preguntaUnoFormularioRegistroUsuario;		
  }
  
  private static void cargarRespuesta() {
    List<String> responseText = response.getOutput().getText();
	if (responseText.size() > 0) {
      System.out.println(responseText+"CARGAR+++RESPUESTA"); //imprime los que dice watson	
	}
	// Update the stored context with the latest received from the dialog.
	 context = response.getContext();
	}
  
  /*
  
  private static void detectarInput() {
		if (input != null && input.text() != "") {
			analizarInput();
		}
	}
  
    
  private static void detectarInput() {
		if (input != null && input.text() != "") {
			analizarInput();
		}
	}
  private static void analizarInput() {
		// User confirms saving the reservation
		if (input.text().equals("formulario registro usuario")) {
			context.put("preguntaNombreCompleto", "Escriba su nombre completo");
			context.put("preguntaCedula","Por favor ingrese su número de Identificación (que debe ser alfanumérico).");
			context.put("preguntaCorreo","Por favor ingrese su dirección de correo electrónico.");
			context.put("preguntaFechaNacimiento", "Por favor ingrese su fecha de nacimiento");
			context.put("notificacionRegistroUsuario", "Ha completado el registro de usuario al sistema");
		}else context.put("preguntaNombreCompleto", "Invalido escriba :Menu:");
			
			// no need to clean the action since it does not return back in the context.
		}
	**/
  
  //metodo para saludo
  public static String obtenerSaludo() {
    String saludo="";
	DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
	LocalTime manana=LocalTime.of(00, 00, 00);
	LocalTime tarde=LocalTime.of(12, 00 , 00);
	LocalTime noche=LocalTime.of(18, 00, 00);
	LocalTime actual=LocalTime.now();
	if (actual.isAfter(manana)& actual.isBefore(tarde)) {
	  saludo="Buenos dìas";
	  System.out.println("Es mañana: " + actual.format(timeFormat));				 
	}
	else if (actual.isAfter(manana)& actual.isAfter(tarde)&actual.isBefore(noche) ) {
	  saludo="Buenas tardes";
	  System.out.println("Es Tarde: " + actual.format(timeFormat));
	}
	else if(actual.isAfter(tarde)&actual.isAfter(noche)) {
	   saludo="Buenas noches";
	 System.out.println("Es de noche: " + actual.format(timeFormat));
	}
	  return saludo;
	}
 
  
	}

