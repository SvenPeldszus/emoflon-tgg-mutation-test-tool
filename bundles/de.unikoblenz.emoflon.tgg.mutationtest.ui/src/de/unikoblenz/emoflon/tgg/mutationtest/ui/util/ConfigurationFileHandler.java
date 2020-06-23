package de.unikoblenz.emoflon.tgg.mutationtest.ui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestSerializableConfig;

public class ConfigurationFileHandler {

	private static final Logger LOGGER = Logger.getLogger(ConfigurationFileHandler.class);

	private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + "emoflon"
			+ File.separator + "config.json";

	private Gson gson = new Gson();

	public void saveConfigurationToJsonFile(MutationTestConfiguration configuration) {
		Set<MutationTestSerializableConfig> configs = new HashSet<>();

		if (new File(CONFIG_FILE).exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
				configs = gson.fromJson(br, new TypeToken<HashSet<MutationTestSerializableConfig>>() {
				}.getType());
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		configs.add(new MutationTestSerializableConfig(configuration));

		String json = gson.toJson(configs);
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			writer.write(json);
			writer.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

	public Set<MutationTestSerializableConfig> readConfigsFromFile() {
		Set<MutationTestSerializableConfig> configs = new HashSet<>();

		if (new File(CONFIG_FILE).exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
				configs = gson.fromJson(br, new TypeToken<HashSet<MutationTestSerializableConfig>>() {
				}.getType());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(e.getMessage(), e);
			}
		}
		return configs;
	}

}
