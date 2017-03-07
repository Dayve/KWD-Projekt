package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

public class ID3Controller {
	@FXML private TextField numberOfSubsetsTF;
	@FXML private TextField setPercentageForTreeLowerLimitTF;
	@FXML private TextField setPercentageForTreeUpperLimitTF;
	@FXML private PieChart id3PieChart;
	@FXML private BarChart<String, Double> percentageOfTreesBarChart;
	

	@FXML public void initialize() {
		id3PieChart.setVisible(false);
		percentageOfTreesBarChart.setVisible(false);
	}
	
	@FXML private void generateCharts() {
		int numberOfSubsets;
		double setPercentageForTreeLowerLimit, setPercentageForTreeUpperLimit;

		try {
			numberOfSubsets = Integer.parseInt(numberOfSubsetsTF.getText());
			setPercentageForTreeLowerLimit = Double.parseDouble(setPercentageForTreeLowerLimitTF.getText());
			setPercentageForTreeUpperLimit = Double.parseDouble(setPercentageForTreeUpperLimitTF.getText());
			
			if(setPercentageForTreeLowerLimit > setPercentageForTreeUpperLimit || 
					setPercentageForTreeLowerLimit == 0.0 || setPercentageForTreeUpperLimit == 0.0
					|| setPercentageForTreeUpperLimit > 100.0 ||  setPercentageForTreeUpperLimit <= 0.0 
					|| setPercentageForTreeLowerLimit < 0.0 || numberOfSubsets > 100 
					|| numberOfSubsets <= 0 || setPercentageForTreeLowerLimit < 10.0) {
				throw new NumberFormatException();
			}
		}
		catch(NumberFormatException numFormatEx) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Niewłaściwe parametry");
			alert.setHeaderText(null);
			alert.setContentText("Proszę ustawić poprawne parametry w polach tekstowych "
					+ "(maksymalnie 100 podzbiorów i co najmniej 10% zbioru uczącego).");

			alert.showAndWait();
			return;
		}
		
		// Set up the pie chart:
		
		id3PieChart.setVisible(true);
		
		ID3 id3 = new ID3(numberOfSubsets, setPercentageForTreeLowerLimit, setPercentageForTreeUpperLimit);
		
		String rightDiagnosisChartLabel = "Poprawne: " + String.format("%.2f", id3.getNumberOfCorrectDiagnosis() * 100.0 / 
				id3.getNumberOfAllDiagnosis()) + "% \n(" 
			+ id3.getNumberOfCorrectDiagnosis() + "/" + id3.getNumberOfAllDiagnosis() + ")";
		String wrongDiagnosisChartLabel = "Mylne: " + String.format("%.2f", id3.getNumberOfIncorrectDiagnosis() * 100.0 / 
				id3.getNumberOfAllDiagnosis()) + "% \n("
				+ id3.getNumberOfIncorrectDiagnosis() + "/" + id3.getNumberOfAllDiagnosis() + ")";
		String unknownDiagnosisChartLabel = "Brak: " + String.format("%.2f", id3.getNumberOfUnknownDiagnosis() * 100.0 / 
				id3.getNumberOfAllDiagnosis()) + "% \n("
				+ id3.getNumberOfUnknownDiagnosis() + "/" + id3.getNumberOfAllDiagnosis() + ")";
		
		ObservableList<PieChart.Data> id3DiagnosisData = 
                FXCollections.observableArrayList(
                    new PieChart.Data(rightDiagnosisChartLabel, id3.getNumberOfCorrectDiagnosis()),
                    new PieChart.Data(wrongDiagnosisChartLabel, id3.getNumberOfIncorrectDiagnosis()),
                    new PieChart.Data(unknownDiagnosisChartLabel, id3.getNumberOfUnknownDiagnosis()));
        
		id3PieChart.setTitle("Poprawność diagnoz:");
		id3PieChart.setData(id3DiagnosisData);
		
		// Set up the bar chart:

		// We're switching the visibility on and off here because of the animation:
		percentageOfTreesBarChart.setVisible(false);
		percentageOfTreesBarChart.getData().clear();
		percentageOfTreesBarChart.setVisible(true);
		
		percentageOfTreesBarChart.setTitle("Udział procentowy podzbiorów:");
		
		XYChart.Series<String, Double> sizeSeries = new XYChart.Series<String, Double>();
        sizeSeries.setName("Wielkość zbioru uczącego dla danego drzewa");   
        
        for(int s=0 ; s<numberOfSubsets ; ++s) {
        	sizeSeries.getData().add(new XYChart.Data<String, Double>(
        		new Integer(s+1).toString(), (double)id3.getNthSubsetSize(s)/id3.getWholeLearningSetSize()*100)
        	);
        }
        
        percentageOfTreesBarChart.getData().add(sizeSeries);
	}
}
