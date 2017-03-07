package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {
	private Stage primaryStage;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("ID3 (Dawid Przystasz, Jan Sekułowicz)");
		initID3Layout();
	}
	
	@Override public void stop() {
		Platform.exit();
		System.exit(0);
	}
	
	private void initID3Layout() {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("ID3Layout.fxml"));
		try {
			Parent layout;
			layout = (Parent) loader.load();
			Scene scene = new Scene(layout);
			primaryStage.setResizable(true);
			primaryStage.setScene(scene);
			primaryStage.setWidth(900);
			primaryStage.setHeight(600);
			primaryStage.setMinWidth(800);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
