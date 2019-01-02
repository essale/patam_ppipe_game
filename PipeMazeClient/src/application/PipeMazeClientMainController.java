package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import View.LabelHandler;
import View.LabelStepsHandler;
import View.Theme;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import utils.Converter;
import utils.Rotater;


public class PipeMazeClientMainController implements Initializable {
	
	@FXML
	PipeMazeCanvas pgc;
	
	@FXML
	private Label lbl;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@FXML
	private Label lblSteps;
	
	
	private Theme theme = new Theme("pickleRick");
	private Statistics statistics;
	private int steps = 0;
	private boolean isTimerRunning;
	private boolean isFirsIteration = true;
	private LabelHandler label;
	private LabelStepsHandler stepsLabel;
	private Timer timer;

	private char[][] defaultPipeGame = 
		{
				{'s','-','|','L'},
				{'L','-','|','L'},
				{'L','-','|','L'},
				{'g','-','|','L'},
		};

	
	
	ServerConfig servConfig = new ServerConfig();

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setPipeGameCanvas(defaultPipeGame);
		setLabelHandler();
		setLabelStepsHandler();
		this.statistics = new Statistics();
		this.statistics.setLevel(defaultPipeGame);
		this.statistics.setStepsNumber(0);
		this.timer = new Timer(this.label);
		if(timer != null) {
			startTimer();
			isFirsIteration = false;
			isTimerRunning = true;
		}	 

	}
	
	public void setLabelStepsHandler() {
		this.stepsLabel = new LabelStepsHandler(lblSteps);
	}
	
	public void setLabelHandler() {
		this.label = new LabelHandler(lbl);
	}
	
	public void setPipeGameCanvas(char[][] lvl) 
	{
		pgc.cleanGame();
		pgc.setLevel(lvl,theme);	
		
		if(isFirsIteration == false && !isTimerRunning) {
			resumeTimer(statistics.getSecondsElapsed());
		} 
			
		pgc.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				statistics.setStepsNumber(steps++);
				MouseEvent me = (MouseEvent) event;
				double w = pgc.getWidth() / lvl[0].length; 
				double h = pgc.getHeight() / lvl.length; 
				defaultPipeGame = Rotater.rotate(lvl,(int)Math.floor(me.getY()/h),(int)Math.floor(me.getX()/w));
				Integer inToText = new Integer(statistics.getStepsNumber());
				stepsLabel.setText(inToText.toString());
				setPipeGameCanvas(defaultPipeGame);
			}

		});
	}

	public void openFile() 
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("PipeGame Level File");
		File chosen = fc.showOpenDialog(null);
		if(chosen != null) 
		{
			this.defaultPipeGame = Converter.convertFileToCharArray(chosen);
			setPipeGameCanvas(defaultPipeGame);
			
		}
	}
		
	public void loadLevel() 
	{

		FileChooser fc = new FileChooser();
		fc.setTitle("PipeGame Level File");
		File chosen = fc.showOpenDialog(null);
		if(chosen != null) 
		{
			try {
				this.statistics = objectMapper.readValue(chosen, Statistics.class);
				this.defaultPipeGame = this.statistics.getLevel();
				setPipeGameCanvas(defaultPipeGame);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public void saveState()
	{
		String fileName = new SimpleDateFormat("HHmm_dd_MM_yyyy'.json'").format(new Date());
		
		try {
			objectMapper.writeValue(new File(fileName), this.statistics);
			   Stage popupwindow=new Stage();
		    	popupwindow.initModality(Modality.APPLICATION_MODAL);
		    	popupwindow.setTitle("Level state saved");
		    	Label label1= new Label("Your state of this level just saved to "+System.getProperty("user.dir")+ "\\" + fileName);
		    	VBox layout= new VBox(10);
		    	layout.getChildren().addAll(label1);		    	      
		    	layout.setAlignment(Pos.CENTER);		    	      
		    	Scene scene1= new Scene(layout);
		    	popupwindow.setScene(scene1);	
		    	popupwindow.setWidth(scene1.getWidth()+100);
		    	popupwindow.setHeight(scene1.getHeight()+100);
		    	popupwindow.showAndWait();
		    	
		    //	popupwindow.sizeToScene();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void rickAndMortyTheme(){
		this.theme = new Theme("pickleRick");
		setPipeGameCanvas(defaultPipeGame);
	}
	public void ninjaTurtlesTheme(){
		this.theme = new Theme("ninjaturtle");
		setPipeGameCanvas(defaultPipeGame);
	}
	
	public void resumeTimer(int time) {
		if(!isTimerRunning || isFirsIteration) {
			this.timer = new Timer(label);
			timer.startTimer(time);
		}
	}
	
	public void startTimer() {
		
		if(!isTimerRunning || isFirsIteration) {
			this.timer = new Timer(label);
			if(statistics.getSecondsElapsed() > 0)
				timer.startTimer(statistics.getSecondsElapsed());
			else 
				timer.startTimer();
		}
	}
	
	public void stop(){
		statistics.setSecondsElapsed(timer.getTime());
		timer.stopTimer();
		isTimerRunning = false;
	}
	
	public void start() {
		if(!isTimerRunning) {
			startTimer();
		}
		isTimerRunning = true;
	}
	
	public void restart() {
		stop();
		sleep();
		this.statistics.setLevel(defaultPipeGame);
		this.statistics.setStepsNumber(0);
		this.statistics.setSecondsElapsed(0);
		stepsLabel.setText("0");
		timer.setTimeString("0");
	}
	//public void 	
	
	
	 public void SetServerConfig() throws InterruptedException{
   	  Dialog<ServerConfig> dialog = new Dialog<>();
   	  dialog.setTitle("Server Configuration");
   	  dialog.setHeaderText("Enter server IP number and port number please.");
   	  dialog.setResizable(true);

   	  Label label1 = new Label("IP number: ");
   	  Label label2 = new Label("Port number: ");
   	  TextField text1 = new TextField();
   	  TextField text2 = new TextField();
   	  		
   	  GridPane grid = new GridPane();
   	  grid.add(label1, 1, 1);
   	  grid.add(text1, 2, 1);
   	  grid.add(label2, 1, 2);
   	  grid.add(text2, 2, 2);
   	  dialog.getDialogPane().setContent(grid);
   	  		
   	  ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
   	  dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
   	//  this.servConfig.setServerIP(text1.getText());
   	//  this.servConfig.setPortNumber(Integer.parseInt(text2.getText()));
   	// dialog.setResult(this.servConfig);

   	  dialog.setResultConverter(new Callback<ButtonType, ServerConfig>() {
   	      @Override
   	      public ServerConfig call(ButtonType b) {

   	          if (b == buttonTypeOk) {
   	              return new ServerConfig(text1.getText(), Integer.parseInt(text2.getText()));
   	          }

   	          return null;
   	      }
   	  });
   	  
   	  		
   	  Optional<ServerConfig> result = dialog.showAndWait();
   	  	
   	  if (result.isPresent()) {
   		  this.servConfig.setPortNumber(result.get().getPortNumber());
   		  this.servConfig.setServerIP(result.get().getServerIP());
   	  } 
     }
     
	public void solve() throws InterruptedException {
		stop();
		//ServerConfig serv = new ServerConfig();
		try
	    {
	      Socket theServer = new Socket(this.servConfig.getServerIP(), this.servConfig.getPortNumber());
	      PrintWriter out = new PrintWriter(theServer.getOutputStream());
	      char[][] data = defaultPipeGame;
	      for (int i = 0; i < data.length; i++)
	        out.println(new String(data[i]));
	      out.println("done");
	      out.flush();
	      BufferedReader in = new BufferedReader(new java.io.InputStreamReader(theServer.getInputStream()));
	      String line;
	      
	  	Timeline sortLoop = new Timeline();
		double curDelay = 0.2;
		
	      while (!(line = in.readLine()).equals("done")) { 
	        int i = Integer.parseInt(line.split(",")[0]); //rows
	        int j = Integer.parseInt(line.split(",")[1]); // cols
	        int times = Integer.parseInt(line.split(",")[2]); //rotations
	        //board.switchCell(i, j, times);
	        int rot = 0;
	        while( rot < times) {
				KeyFrame kf = new KeyFrame(Duration.seconds(curDelay), actionEvent -> {
					defaultPipeGame = Rotater.rotate(defaultPipeGame,i,j);
					setPipeGameCanvas(defaultPipeGame);		
				});
				rot++;
				curDelay += 0.2;
	            sortLoop.getKeyFrames().add(kf);
			}
	      }
	      sortLoop.play();
	      in.close();
	      out.close();
	      theServer.close();
	      
	    } 
		catch (java.io.IOException e) {
			e.printStackTrace();
           Stage popupwindow=new Stage();
	    	popupwindow.initModality(Modality.APPLICATION_MODAL);
	    	popupwindow.setTitle("Server error");
	    	Label label1= new Label("Wrong IP address or port number, configure server on 'Server config' button.");
	    	VBox layout= new VBox(10);
	    	layout.getChildren().addAll(label1);		    	      
	    	layout.setAlignment(Pos.CENTER);		    	      
	    	Scene scene1= new Scene(layout, 530, 80);	    	      
	    	popupwindow.setScene(scene1);		    	      
	    	popupwindow.showAndWait();	    	
	    }
	}     
	
	private void sleep() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}



