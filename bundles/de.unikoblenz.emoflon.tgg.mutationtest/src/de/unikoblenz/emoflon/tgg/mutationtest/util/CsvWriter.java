package de.unikoblenz.emoflon.tgg.mutationtest.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvWriter {
	
	private String convertToCSV(String[] data) {
	    return Stream.of(data)
	      .collect(Collectors.joining(","));
	}
	
	public void writeCsvFile(String projectName, List<String[]> dataLines) throws IOException {
		String csvFile = System.getProperty("user.home") + File.separator + "emoflon" + File.separator + projectName + "_testResults" + System.currentTimeMillis() + ".csv";
	    File csvOutputFile = new File(csvFile);
	    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
	        dataLines.stream()
	          .map(this::convertToCSV)
	          .forEach(pw::println);
	    }
	}
}
