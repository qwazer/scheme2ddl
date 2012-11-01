package com.googlecode.scheme2ddl;


import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;

import java.io.File;
import java.util.List;

/**
 * @author A_Reshetnikov
 * @since Date: 16.10.2012
 */
public class UserObjectWriter implements ItemWriter<UserObject> {

    private static final Log log = LogFactory.getLog(UserObjectWriter.class);
    private String outputPath;

    public void write(List<? extends UserObject> data) throws Exception {
        if (data.size() > 0) {
            writeUserObject(data.get(0));
        }
    }

    public void writeUserObject(UserObject userObject) throws Exception {
        String absoluteFileName = outputPath + "/" + userObject.getFileName();
        absoluteFileName = FilenameUtils.separatorsToSystem(absoluteFileName);
        File file = new File(absoluteFileName);
        FileUtils.writeStringToFile(file, userObject.getDdl());
        log.info(String.format("Saved %s %s to file %s",
                userObject.getType().toLowerCase(),
                userObject.getName().toLowerCase(),
                file.getAbsolutePath()));
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
