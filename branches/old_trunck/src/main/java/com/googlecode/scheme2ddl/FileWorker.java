/*
 *    Copyright (c) 2011 Reshetnikov Anton aka qwazer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.googlecode.scheme2ddl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Reshetnikov AV resheto@gmail.com
 * Date: 19.02.11
 * Time: 18:46
 */
public class FileWorker {

    private String outputPath;
    private String fileExtension;
    private Boolean sortByDirectory;


    public void save2file(UserObject obj) {
        try {

            String filePath = createFullFileName(obj);
            FileUtils.writeStringToFile(new File(filePath), obj.getDdl());
            System.out.println("saved " + obj.getType().toLowerCase() + " " + obj.getName().toLowerCase() + " to file " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createFullFileName(UserObject obj) throws IOException {
        String res = "";
        if (sortByDirectory) {
            res = obj.getTypePlural() + "\\" + obj.getName4Filename() + "." + fileExtension;
        } else {
            res = obj.getName4Filename() + "." + obj.getType() + "." + fileExtension;
        }
        res = outputPath + res;
        return FilenameUtils.separatorsToSystem(res);
    }

    public void setSortByDirectory(Boolean sortByDirectory) {
        this.sortByDirectory = sortByDirectory;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

}
