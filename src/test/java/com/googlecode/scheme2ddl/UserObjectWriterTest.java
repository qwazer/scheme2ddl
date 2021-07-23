package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.domain.UserObject;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.io.File;

public class UserObjectWriterTest {

    private UserObjectWriter userObjectWriter = new UserObjectWriter();

    @Test
    public void testWriteUserObjectWithUtf8() throws Exception {

        String content = "--你好 Немного текста в UTF-8";
        String outputPath = "output";
        userObjectWriter.setOutputPath(outputPath);
        UserObject userObject = new UserObject();
        userObject.setType("tmp_test");
        userObject.setSchema("tmp_test");
        userObject.setName("content_utf8");
        userObject.setFileName("tmp_test.sql");
        userObject.setDdl(content);
        userObjectWriter.writeUserObject(userObject);

        String fileName = outputPath + "/" +  userObject.getFileName();
        File f = new File(fileName);
        f.deleteOnExit(); //to delete temp file after test over
        Assertions.assertThat(f).hasContent(content);

    }
}