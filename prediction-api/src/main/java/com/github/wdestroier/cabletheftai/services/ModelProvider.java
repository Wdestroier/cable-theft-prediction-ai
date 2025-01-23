package com.github.wdestroier.cabletheftai.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tensorflow.SavedModelBundle;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Objects;

@Configuration
public class ModelProvider {

	private static final String MODEL_ENTRYPOINT = "serve";
	private static final String MODEL_RESOURCE_DIRECTORY = "models";
	private static final String MODEL_DIRECTORY = "alert-prediction-savedmodel";

	@Bean
	public SavedModelBundle model() throws IOException, URISyntaxException {
		var resource = getClass().getClassLoader().getResource(MODEL_RESOURCE_DIRECTORY + '/' + MODEL_DIRECTORY);
		Objects.requireNonNull(resource, "Model resource was not found.");

		return isFileResource(resource)
				? loadModelFromFilesystem(resource)
				: loadModelFromJar(resource);
	}

	private boolean isFileResource(URL resource) {
		return "file".equals(resource.getProtocol());
	}

	private SavedModelBundle loadModelFromFilesystem(URL resource) throws URISyntaxException {
		var path = Paths.get(resource.toURI());
		return SavedModelBundle.load(path.toString(), MODEL_ENTRYPOINT);
	}

	private SavedModelBundle loadModelFromJar(URL resource) throws IOException, URISyntaxException {
		var tempDir = Files.createTempDirectory(MODEL_DIRECTORY);

		// Extract all files and directories from the resource into the tempDir.
		try (var fileSystem = FileSystems.newFileSystem(resource.toURI(), Map.of())) {
			var jarPath = fileSystem.getPath(MODEL_RESOURCE_DIRECTORY + '/' + MODEL_DIRECTORY);

			// Walk through the directory inside the JAR and copy to tempDir.
			Files.walkFileTree(jarPath, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
					// Create corresponding directory in tempDir.
					var targetDir = tempDir.resolve(jarPath.relativize(dir).toString());
					Files.createDirectories(targetDir);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
					// Copy files to corresponding location in tempDir.
					var targetFile = tempDir.resolve(jarPath.relativize(file).toString());
					Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}
			});
		}

		// Load the TensorFlow model from the extracted temporary directory.
		return SavedModelBundle.load(tempDir.toString(), MODEL_ENTRYPOINT);
	}

}
