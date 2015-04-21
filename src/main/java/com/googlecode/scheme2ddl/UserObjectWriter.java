package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.domain.UserObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;

import java.io.*;
import java.nio.charset.*;
import java.util.List;

/**
 * @author A_Reshetnikov
 * @since Date: 16.10.2012
 */
public class UserObjectWriter implements ItemWriter<UserObject> {

	private static final Log log = LogFactory.getLog(UserObjectWriter.class);
	private String outputPath;
	private String encoding = Charset.defaultCharset().name();
	private boolean cleanOutputDirectory = false;

	private boolean executed = false;

	public void write(List<? extends UserObject> data) throws Exception {
		synchronized (UserObjectWriter.class) {
			if (!executed) {
				if (cleanOutputDirectory)
					cleanOutputDirecory();

				executed = true;
			}
		}

		if (data.size() > 0) {
			writeUserObject(data.get(0));
		}
	}

	public void writeUserObject(UserObject userObject) throws Exception {
		String absoluteFileName = outputPath + "/" + userObject.getFileName();
		absoluteFileName = FilenameUtils.separatorsToSystem(absoluteFileName);
		File file = new File(absoluteFileName);
		FileUtils.writeStringToFile(file, userObject.getDdl(), encoding);
		log.info(String.format("Saved %s %s.%s to file %s", userObject
				.getType().toLowerCase(), userObject.getSchema().toLowerCase(),
				userObject.getName().toLowerCase(), file.getAbsolutePath()));
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getOutputPath() {
		return this.outputPath;
	}

	public void setEncoding(String encoding) {
		if (Charset.isSupported(encoding)) {
			this.encoding = encoding;
		} else {
			this.encoding = Charset.defaultCharset().name();
			log.warn(String.format(
					"Encoding '%s' is not supported. It's changed to %s",
					encoding, this.encoding));
		}
	}

	public void setCleanOutputDirectory(boolean cleanOutputDirectory) {
		this.cleanOutputDirectory = cleanOutputDirectory;
	}

	@Deprecated
	public void setFileNameCase(String fileNameCase) {
		// for compatability with 2.1.x config
	}

	@Deprecated
	public void setIncludeSchemaName(boolean includeSchemaName) {
		// for compatability with 2.1.x config
	}

	private void cleanOutputDirecory() {
		try {
			File file = new File(outputPath);
			if (file.exists() && file.isDirectory())
				FileUtils.cleanDirectory(file);
		} catch (IOException ex) {
			log.error("Cann't clean output direcory", ex);
		}
	}
}
