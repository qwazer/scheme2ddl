package com.googlecode.scheme2ddl;

/**
 * @author A_Reshetnikov
 * @since Date: 16.10.2012
 */

import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;

import java.io.File;
import java.util.List;

/**
 * Dummy {@link org.springframework.batch.item.ItemWriter} which only logs data it receives.
 */
public class UserObjectWriter implements ItemWriter<UserObject> {

    private static final Log log = LogFactory.getLog(UserObjectWriter.class);
    private String outputPath;

    /**
     * @see org.springframework.batch.item.ItemWriter#write(Object)
     */
    public void write(List<? extends UserObject> data) throws Exception {
        if (data.size() > 0) {
            writeUserObject(data.get(0));
        }
        log.info(data);
    }

    public void writeUserObject(UserObject userObject) throws Exception {
        String absoluteFileName = outputPath + "/" + userObject.getFileName();
        absoluteFileName = FilenameUtils.separatorsToSystem(absoluteFileName);
        FileUtils.writeStringToFile(new File(absoluteFileName), userObject.getDdl());
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
