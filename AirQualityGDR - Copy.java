// AirQualityGDR		Author: Garrett Reihner
// 
// A graphics application that visualizes daily air quality in Western PA
// neighborhoods and allows navigation based on date, air quality parameter,
// summative air quality assessments, and region.

// Data Source: https://data.wprdc.org/dataset/allegheny-county-air-quality

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;
import java.util.Scanner;
import java.io.*;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import java.util.Arrays;
import javafx.scene.input.MouseEvent;

/*
Windows commands for compiling, running:
javac --module-path "C:\Users\reihn\Documents\javafx-sdk-20.0.2\lib" --add-modules javafx.controls AirQualityGDR.java
java --module-path "C:\Users\reihn\Documents\javafx-sdk-20.0.2\lib" --add-modules javafx.controls AirQualityGDR


javac --module-path %PATH_TO_FX% --add-modules javafx.controls AirQualityXXX.java
java --module-path %PATH_TO_FX% --add-modules javafx.controls AirQualityXXX
*/

public class AirQualityGDR extends Application {
	// filename in current directory to use as data source
	// private final String filename = "airQualitySmall.txt";
	// private final String filename = "airQualitySynthetic.txt";
	// private final String filename = "airQualityModerate.txt";
	// private final String filename = "airQualityLarge.txt";
	// private final String filename = "airQualityVeryLarge.txt";
	private final String filename = "airQualityFull.txt";
	
	// constants to define a 600x600 plotsize with 50 pixel buffer on all sides
	private final int PLOTSIZE = 800;
	private final int BUFFER = 50;
	
	private int maxYear;	// most recent year appearing in file
	private int minYear;	// oldest year appearing in file
	private int maxReadings;	// most readings of one type to appear in file
	
	
// UNCOMMENT THE FOLLOWING LINE OF CODE ONCE YOU HAVE CREATED A ReadingXXX.java FILE
	private ReadingGDR[] readings;	// array to store all data read out of file
	private Label[] parameters;		// unique parameter names as labels for bars
	private Rectangle[] bars;		// bars for each parameter to display in chart
	private String[] sites;			// unique site names appearing in file

	// GUI elements for radio buttons to select all data or data filtered by
	// air quality description based on the measured parameter
	private ToggleGroup qualitySelect;
	private RadioButton allButton, goodButton, moderateButton, unhealthyButton;
	
	// GUI elements for text field to select data based on year
	private Label yearLabel;
	private TextField yearSelect;
	
	private Label showing;	// report which (sub)set of the data is being shown
	private Label details;	// report number of readings represented by clicked bar

    public void start(Stage primaryStage) {

		
		// set values for numReadings, minYear, and maxYear
		// minYear cannot be 0 since it would always be lower than any year
		int numReadings = 0;
		minYear = 10000;
		maxYear = 0;
		
		// create an array of unique sites, assuming there are never more than
		// 100 different sites, based on specification. also create a numSites
		// variable to tell how long the sites array should be
		String[] foundSites = new String[100];
		int numSites = 0;
		
		// create an array of unique parameters, again assuming never more than
		// 100. Also creates an array of the counts for each parameter with
		// the same assumption. creates a numParameters to determine the length
		// of the parameters and bars arrays.
		String[] foundParameters = new String[100];
		int numParameters = 0;
		int[] countParameters = new int[100];

		// try-catch block for first pass through reading the data file
		try {
			Scanner filescan = new Scanner(new File(filename));	
			// set delimiter to help deal with spaces
			filescan.useDelimiter("\\t|\\n");
			
			// skip header
			filescan.nextLine();
			
			// set up while loop for reading the first pass
			while(filescan.hasNext()) {
				filescan.next(); // skip _id
				String yearString = filescan.next(); // read in the whole date
				String site = filescan.next(); // read in the site
				String parameter = filescan.next(); // read in parameter
				filescan.next(); // skip index_value
				filescan.next(); // skip description
				filescan.next(); // skip health_advisory
				filescan.next(); // skip health_effects
				
				//update the number of readings
				numReadings++;
				
				//extract year by taking a substring of the characters after
				//the last "/" and parsing it to an integer
				int year = Integer.parseInt(yearString.substring(
						yearString.lastIndexOf("/")+1));
				minYear = Math.min(minYear, year);
				maxYear = Math.max(maxYear, year);
				
				// find the unique sites. a boolean is required here so that
				// null values are not compared
				boolean foundSite = false;
                for (int i = 0; i < foundSites.length; i++) {
                    if (foundSites[i] != null && foundSites[i].equals(site)) {
                        foundSite = true;
                    }
                }
				// assign sites to the foundSites array if it is not already
				// and increment the number of unique sites
                if (!foundSite) {
                    foundSites[numSites] = site;
                    numSites++;
                }
				
				// find the unique parameters, again needing a boolean for the
				// same reason to avoid checking null values
				boolean foundParameter = false;
                for (int i = 0; i < foundParameters.length; i++) {
                    if (foundParameters[i] != null 
							&& foundParameters[i].equals(parameter)) {
                        foundParameter = true;
						// increment that specific parameter by 1 each time it 
						// is found in the file
                        countParameters[i]++;
                    }
                }
				// assign parameters to the foundParameters array
				// and set the count to 1 initially
                if (!foundParameter) {
                    foundParameters[numParameters] = parameter;
                    countParameters[numParameters] = 1; 
					// increment numParameters to avoid replacing parameters
                    numParameters++;
				}
			}
		} catch (IOException e) {System.out.println(e);};
		
		// find the highest count for any of the parameters
		// by looping through the counts
		maxReadings = 0;
        for (int i = 0; i < numParameters; i++) {
            if (countParameters[i] > maxReadings) {
                maxReadings = countParameters[i];
            }
        }
		
		// after the first pass over the data file is fully processed, each
		// of the following facts should be able to be printed out for
		// testing purposes
		System.out.println("Number of readings: " + numReadings);
		System.out.println("Earliest year: " + minYear);
		System.out.println("Latest year: " + maxYear);
		System.out.println("Most readings per parameter: " + maxReadings);
		System.out.println("Number of unique sites: " + numSites);
		System.out.println("Number of unique parameters: " + numParameters);
		
		// initialize and instantiate the unique sites
		// being sure to use the numSites so it doesn't assign any of the nulls
		sites = new String[numSites];
		for (int i = 0; i < sites.length; i++) {
			sites[i] = foundSites[i];
		}
		
		// instantiate and initialize the unique parameters
		// using numParameters to avoid assigning any nulls
		parameters = new Label[numParameters];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new Label(foundParameters[i]);
		}
		
		// instantiate and initialize the bars
		bars = new Rectangle[numParameters];
		// defining the spacing and the width outside loop since it does not
		// require information about any specific index
        double barSpacing = 5;
		double barWidth = ((double) PLOTSIZE - 
				(bars.length - 1)*barSpacing)/bars.length;
		// loop through to assign rectangle objects to bars from countParameters
		for (int i = 0; i < bars.length; i++) {
			// height has to be defined within the loop since it requires
			// a specific index
			double barHeight = (countParameters[i] / (double) maxReadings) * 
					(PLOTSIZE);
			// initialize the new rectangle object with specific parameters
			// using the constructor Rectangle(x, y, width, height)
            bars[i] = new Rectangle(BUFFER + (barWidth + barSpacing) * i, 
					PLOTSIZE + BUFFER - barHeight,
					barWidth,
					barHeight); 
			
			// add the text from the parameters to label the bars
			parameters[i].setText(foundParameters[i]);
            parameters[i].setLayoutX(BUFFER + (barWidth + barSpacing) * i); 
			// we want the y position to be in the middle of the bottom buffer
            parameters[i].setLayoutY(PLOTSIZE + BUFFER*1.5);
        }
		
		// instantiate the array of readings from the number of readings
		readings = new ReadingGDR[numReadings];
		
		// try-catch block for second pass through the file
		try {
			Scanner filescan = new Scanner(new File(filename));		
			filescan.useDelimiter("\\t|\\n");
			
			//skip the header
			filescan.nextLine();
			
			// since we know the numReadings now (which is the same as the
			// length of the readings array), we can use a for loop for better
			// design choices
			for(int i = 0; i < readings.length; i++) {
				// begin reading through the file, this time not skipping
				int id = filescan.nextInt(); // extract _id
				String date = filescan.next(); // year parsed to int below
				String site = filescan.next(); // extract site
				String parameter = filescan.next(); // extract parameter
				int indexValue = filescan.nextInt(); // extract the index. 
				// indexValue can be removed if desired, but assuming a wide
				// range of uses, it is included in the object class
				String description = filescan.next(); // extract description
				
				String healthAdvisory = filescan.next();
				String healthEffects = filescan.next();
				// the 2 above lines read in the health_advisory and
				// health_effects. these are not necessary in this visualization
				// but are part of the object class to accomodate for various
				// use cases.
				
				// parse the year to an int
				int year = Integer.parseInt(date.substring(
						date.lastIndexOf("/")+1));
				
				// Create a ReadingGDR object and populate its fields
				ReadingGDR reading = new ReadingGDR(id, year, site, parameter, 
						indexValue, description, healthAdvisory, healthEffects);
				// assign the new ReadingGDR object to the array of readings
				readings[i] = reading;
				
			}
			
		} catch (IOException e) {System.out.println(e);};
		
		
		// create the ToggleGroup for the radio buttons and associates all
		// four RadioButton objects with this ToggleGroup
		qualitySelect = new ToggleGroup();
		allButton = new RadioButton("All");
		allButton.setToggleGroup(qualitySelect);
		allButton.setSelected(true);
		allButton.setTranslateX(10);
		allButton.setTranslateY(10);
		// attach event handler for radio button
		allButton.setOnAction(this::radioAction);
		goodButton = new RadioButton("Good");
		goodButton.setToggleGroup(qualitySelect);
		goodButton.setTranslateX(50);
		goodButton.setTranslateY(10);
		// attach event handler for radio button
		goodButton.setOnAction(this::radioAction);
		moderateButton = new RadioButton("Moderate");
		moderateButton.setToggleGroup(qualitySelect);
		moderateButton.setTranslateX(110);
		moderateButton.setTranslateY(10);
		// attach event handler for radio button
		moderateButton.setOnAction(this::radioAction);
		unhealthyButton = new RadioButton("Unhealthy");
		unhealthyButton.setToggleGroup(qualitySelect);
		unhealthyButton.setTranslateX(190);
		unhealthyButton.setTranslateY(10);
		// attach event handler for radio button
		unhealthyButton.setOnAction(this::radioAction);
		
		MenuButton siteMenu = new MenuButton("Site");
		siteMenu.setTranslateX(290);
		siteMenu.setTranslateY(5);

		// this loop add each MenuItem object to the MenuButton and attaches
		// the shared siteAction event handler to all MenuItem objects
		for (int i=0; i<sites.length; i++) {
			MenuItem newItem = new MenuItem(sites[i]);
			siteMenu.getItems().add(newItem);
			newItem.setOnAction(this::siteAction);
		}
		
		Label yearLabel = new Label("Year: ");
		yearLabel.setTranslateX(370);
		yearLabel.setTranslateY(10);
		
		yearSelect = new TextField();
		yearSelect.setPrefWidth(50);
		yearSelect.setTranslateX(410);
		yearSelect.setTranslateY(5);
		// attach event handler for Year text field
		yearSelect.setOnAction(this::yearAction);
		
		showing = new Label("All Readings");
		showing.setTranslateX(500);
		showing.setTranslateY(10);

		details = new Label("");
		details.setTranslateX(500);
		details.setTranslateY(25);

		Group root = new Group(allButton, goodButton, moderateButton,
			unhealthyButton, siteMenu, yearLabel, yearSelect, showing, details);

		for (int i=0; i<parameters.length; i++) {
			root.getChildren().add(parameters[i]);
			root.getChildren().add(bars[i]);
		}
		
        Scene scene = new Scene(root, PLOTSIZE+BUFFER*2, PLOTSIZE+BUFFER*2, 
				Color.WHITE);
		// The following line attaches the event handler "mousePressed" to the 
		// scene to be called when there is a mouse click within the window.
		scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);		
        primaryStage.setTitle("Air Quality");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
	
	//**********************START OF HELPER METHODS****************************
	
	// helper method to filter the data based on a specific description
	// designed to work for all descriptions
	private ReadingGDR[] filterReadingsDesc(String description) {
		// initialize a count for the number of instances of that description
		// and loop through the array of ReadingGDRs
		int count = 0; 
		for (int i = 0; i < readings.length; i++) {
			if (readings[i].getDescription().equals(description)) {
				count++;
			}
		}
		// create a new array of the filtered data so as to not override the 
		// original array. then loop through the original array to assign 
		// ReadingGDR objects to the new array where the description matches
		ReadingGDR[] filteredReadings = new ReadingGDR[count];
		int index = 0;
		for (int i = 0; i < readings.length; i++) {
			if (readings[i].getDescription().equals(description)) {
				filteredReadings[index] = readings[i];
				index++;
			}
		}
		return filteredReadings;
	}
	
	// helper method to filter based on the site. This has to be different
	// and include an array of ReadingGDR objects because it has to be able
	// to function alongside the description filter. this allows it to
	// filter the data for a specific description
	private ReadingGDR[] filterReadingsSite(ReadingGDR[] readingInput, 
			String site) {
		// as in the previous method, loop through the array to find the count
		// of instances where a specific site occurs within that subset of data.
		// it is different than previous because it loops through the already
		// filtered data based on description rather than the whole dataset
		// assuming the "All" radio button is not selected.
		int count = 0; 
		for (int i = 0; i < readingInput.length; i++) {
			if (readingInput[i].getSite().equals(site)) {
				count++;
			}
		}
		// now create a new array with the twice filtered data. then loop
		// through the filtered dataset to assign objects to the new array
		// where the site matches
		ReadingGDR[] filteredReadings = new ReadingGDR[count];
		int index = 0;
		for (int i = 0; i < readingInput.length; i++) {
			if (readingInput[i].getSite().equals(site)) {
				filteredReadings[index] = readingInput[i];
				index++;
			}
		}
		return filteredReadings;
	}
	
	// helper method to filter based on the year. Same reasoning as the method
	// to filter based on site as to why a ReadingGDR[] is an argument
	private ReadingGDR[] filterReadingsYear(ReadingGDR[] readingInput, int year) {
		// same loop as before, but counting based on a specific year.
		int count = 0; 
		for (int i = 0; i < readingInput.length; i++) {
			// reminder that you can use == here since you are comparing ints
			if (readingInput[i].getYear() == year) {
				count++;
			}
		}
		// again create a new array for the twice filtered data, looping through
		// to assign where the year matches
		ReadingGDR[] filteredReadings = new ReadingGDR[count];
		int index = 0;
		for (int i = 0; i < readingInput.length; i++) {
			if (readingInput[i].getYear() == year) {
				filteredReadings[index] = readingInput[i];
				index++;
			}
		}
		return filteredReadings;
	}
	
	// helper method to set the height of the bars. I originally was going to
	// follow the suggestion of the document and name it setHeights, but I
	// wanted to make it more explicit since the Rectangle class has a setHeight
	// method and could potentially be confused. One additional note is that I
	// decided to design it to take the whole array of ReadingGDR objects rather
	// than just an int[], This was to avoid some redundancy since calculating
	// the counts would be necessary for everything, so my choices were
	// 1) incorporate it in each helper method
	// 2) create a specific countInstances helper method
	// 3) incorporate it here
	// since I would always want to use that code alongside this method, I
	// chose to incorporate it here for easier readability
	private void setVisualHeight(ReadingGDR[] readingSet) {
		// loop through the input array to count the number of instances for 
		// each parameter. this requires a nested loop: one to loop through 
		// each parameter, and one to loop through the full filtered array
		// of ReadingGDR objects. It then increments the counter for that
		// specific parameter
		int[] filteredCounts = new int[parameters.length];
		for (int i = 0; i < readingSet.length; i++) {
			for (int j = 0; j < parameters.length; j++) {
				if (readingSet[i].getParameter().equals(
						parameters[j].getText())) {
					filteredCounts[j]++;
				}
			}
		}
		// loop through the parameters and set the heights and the y position
		for (int i = 0; i < parameters.length; i++) {
			double barHeight = (filteredCounts[i] / 
					(double) maxReadings) * (PLOTSIZE); 
			bars[i].setHeight(barHeight);
			bars[i].setY(PLOTSIZE + BUFFER - barHeight); 
		} 
	}
	
	//************************END OF HELPER METHODS****************************
	
	// this is a single event handler for all of the radio buttons. I spent some
	// time with the API looking at the ToggleGroup and found that instead of 
	// having an event handler for each radio button, I could just extract which
	// radio button was active and filter based on that.
	private void radioAction(ActionEvent event) {
		// find the active radio button
		RadioButton selectedRadioButton = (RadioButton) 
				qualitySelect.getSelectedToggle();
		String activeButton = selectedRadioButton.getText();
		
		// filter the data based on the active button and set the new heights
		// for the bars. 
		if (!activeButton.equals("All")) {
			ReadingGDR[] filteredReadings = filterReadingsDesc(activeButton);
			showing.setText(selectedRadioButton.getText() + " Readings");
			setVisualHeight(filteredReadings);
		} else {
			showing.setText("All Readings");
			setVisualHeight(readings);
		}
		
		// removes the details text. could use an if statement, but it
		// would either have to evaluate the if statement as true and then
		// change it or evaluate it as false, and there is no harm in
		// setting it as nothing if it is already nothing. This will be included
		// in the subsequent event handlers
		details.setText("");
	}
	
	// this is the event handler for the site dropdown. it is designed in a
	// similar way to the radio buttons so that it utilizes the ToggleGroup
	// to determine how to filter the data and set the showing text
	private void siteAction(ActionEvent event) {
		// called when a MenuItem is selected
		// the following line checks which MenuItem was selected out of the menu
		String selection = ((MenuItem)(event.getSource())).getText();
		ReadingGDR[] filteredReadings = readings; // Start with all readings
		
		// check which filter is currently active
		RadioButton selectedRadioButton = (RadioButton) 
				qualitySelect.getSelectedToggle();
		String activeButton = selectedRadioButton.getText();
		
		// filter the data based on the description first and set the 
		// showing text to be "(description) (site) Readings"
		if (!activeButton.equals("All")) {
			filteredReadings = filterReadingsDesc(activeButton);
			showing.setText(selectedRadioButton.getText() + " " 
					+ selection + " Readings");
		} else {
			showing.setText(selectedRadioButton.getText() + " " 
					+ selection + " Readings");
		}
		
		
		// Filter readings by site based on the data filtered by description
		filteredReadings = filterReadingsSite(filteredReadings, selection); 
		// Update the visualization
		setVisualHeight(filteredReadings);
		details.setText("");
	}
	
	// event handler for the year text field. it is also designed to utilize
	// the ToggleGroup to determine how to filter. It uses a try-catch block
	// to make sure that 1) the input is an integer, and 2) it is within range
	// of the data. I chose a try-catch block so that it could easily 
	// clear the text box and not throw an error when a non-integer was input.
	private void yearAction(ActionEvent event) {
		String yearString = yearSelect.getText();
		ReadingGDR[] filteredReadings = readings;
		details.setText("");
		try {
			int yearSelected = Integer.parseInt(yearString);
			
			// check if the input is in range
			if (yearSelected >= minYear && yearSelected <= maxYear) {
				
				// check which description filter is currently active
				RadioButton selectedRadioButton = (RadioButton) 
						qualitySelect.getSelectedToggle();
				String activeButton = selectedRadioButton.getText();
				
				// filter by description based on the active radio button
				// and change the showing text
				if (!activeButton.equals("All")) {
					filteredReadings = filterReadingsDesc(activeButton);
					showing.setText(selectedRadioButton.getText() + " " 
							+ yearSelected + " Readings");
				} else {
					showing.setText(
							selectedRadioButton.getText() + " " 
							+ yearSelected + " Readings");
				}
				
				// Filter readings by year
				filteredReadings = filterReadingsYear(filteredReadings, 
						yearSelected);
				// Update the visualization
				setVisualHeight(filteredReadings);
			}
		} catch (Exception e) {}; 
		// clear the field 
		yearSelect.setText("");
	}
	
	private void mousePressed(MouseEvent event) {
        // get the x and y coordinate of the mouse click
        double pressX = event.getX();
		double pressY = event.getY();
		
		ReadingGDR[] filteredReadings = readings;
		
		// check which description filter is currently active
		RadioButton selectedRadioButton = (RadioButton) 
				qualitySelect.getSelectedToggle();
		String activeButton = selectedRadioButton.getText();
		
		// filter the data based on description
		if (!activeButton.equals("All")) {
			filteredReadings = filterReadingsDesc(activeButton);
		}
		
		// I am aware there should be a check to filter by site and year, but
		// I was unable to achieve this properly without a lot of bad design
		// choices such as lots of string manipulation to check the showing text
		// to see if it contained integers since the text field gets cleared
        
		// calculate the width of each bar
        double barWidth = ((double) PLOTSIZE - 
				(bars.length - 1) * 5) / bars.length;
        // find the index of the clicked bar, setting it outside the range
		// and checking if the x and y coordinates of the click correspond
		// to pixels within a bar and determines what bar
        int clickedBarIndex = -1; 
        for (int i = 0; i < bars.length; i++) {
            double barStartX = BUFFER + (barWidth + 5) * i;
            double barEndX = barStartX + barWidth; 
			double barY = PLOTSIZE + BUFFER - bars[i].getHeight(); 
			double barTopY = PLOTSIZE + BUFFER;
            if (pressX >= barStartX && pressX <= barEndX 
				&& pressY >= barY && pressY <= barTopY) {
				clickedBarIndex = i;
			}
        }

        // update the details label
        if (clickedBarIndex != -1) {
            // calculate the count for the clicked parameter bar
            int clickedBarCount = 0;
			// loop through the filtered array to count the instances
			// of that parameter
            for (int i = 0; i < filteredReadings.length; i++) {
                if (filteredReadings[i].getParameter().equals(
						parameters[clickedBarIndex].getText())) {
                    clickedBarCount++;
                }
            }
            details.setText("Readings for " 
					+ parameters[clickedBarIndex].getText() 
					+ ": " + clickedBarCount);
        } else {
            details.setText(""); 
        }
    }
	
    public static void main(String[] args)
    {
        launch(args);
    }
}
