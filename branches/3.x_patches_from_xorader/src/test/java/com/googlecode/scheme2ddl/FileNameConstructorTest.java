package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.domain.UserObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.scheme2ddl.FileNameConstructor.abbreviate;
import static com.googlecode.scheme2ddl.FileNameConstructor.pluralaze;
import static org.junit.Assert.assertEquals;

/**
 * @author A_Reshetnikov
 * @since Date: 03.05.2013
 */

public class FileNameConstructorTest {
    private FileNameConstructor fileNameConstructor;
    private List<UserObject> list;

    @Before
    public void setUp() throws Exception {
        this.fileNameConstructor = new FileNameConstructor();
        fileNameConstructor.afterPropertiesSet();
        this.list = new ArrayList<UserObject>();
        UserObject userObject = new UserObject();
        userObject.setName("SimpleName");
        userObject.setType("Table");
        userObject.setSchema("SCHEMA_Name");
        list.add(userObject);

        userObject = new UserObject();
        userObject.setName("SYS$_#NAME");
        userObject.setType("REFRESH GROUP");
        userObject.setSchema("");
        list.add(userObject);

        userObject = new UserObject();
        userObject.setName("StraNge_NAME$");
        userObject.setType("Index");
        userObject.setSchema("dummy");
        list.add(userObject);

        userObject = new UserObject();
        userObject.setName("lib01");
        userObject.setType("library");
        userObject.setSchema("dummy");
        list.add(userObject);

        userObject = new UserObject();
        userObject.setName("TYPE_01");
        userObject.setType("TYPE");
        userObject.setSchema("TYPES");
        list.add(userObject);

        userObject = new UserObject();
        userObject.setName("oracle/i18n/data/lx10039.glb");
        userObject.setType("JAVA RESOURCE");
        userObject.setSchema("SYS");
        list.add(userObject);


    }

//    @org.junit.Test
//    public void testMap2FileName() throws Exception {
//
//    }

    @Test
    public void testDefaultTemplate() throws Exception {
        for (UserObject userObject : list) {
            String fileName = pluralaze(abbreviate(userObject.getType())).replace(" ", "_") + "/" + userObject.getName() + ".sql";
            fileName = fileName.toLowerCase();
            assertEquals(fileName, fileNameConstructor.map2FileName(userObject));
        }
    }

    @Test
    public void testKeywordSchema() throws Exception {
        String template = FileNameConstructor.kw_schema_lower + "/" + FileNameConstructor.kw_schema_UPPER;
        fileNameConstructor.setTemplate(template);
        fileNameConstructor.afterPropertiesSet();
        for (UserObject userObject : list) {
            String fileName = userObject.getSchema().toLowerCase() + "/" + userObject.getSchema().toUpperCase();
            assertEquals(fileName, fileNameConstructor.map2FileName(userObject));
        }
    }

    @Test
    public void testKeywordObjectName() throws Exception {
        String template = FileNameConstructor.kw_objectname_lower + "/" + FileNameConstructor.kw_objectname_UPPER;
        fileNameConstructor.setTemplate(template);
        fileNameConstructor.afterPropertiesSet();
        for (UserObject userObject : list) {
            String fileName = userObject.getName().toLowerCase() + "/" + userObject.getName().toUpperCase();
            assertEquals(fileName, fileNameConstructor.map2FileName(userObject));
        }
    }

    @Test
    public void testKeywordExtension() throws Exception {
        String template = FileNameConstructor.kw_extension_lower + "/" + FileNameConstructor.kw_extension_UPPER;
        fileNameConstructor.setTemplate(template);
        fileNameConstructor.afterPropertiesSet();
        for (UserObject userObject : list) {
            String fileName = "sql" + "/" + "sql".toUpperCase();
            assertEquals(fileName, fileNameConstructor.map2FileName(userObject));
        }
    }

    @Test
    public void testTemplateWithTypeMix() throws Exception {
        String template = FileNameConstructor.kw_type_lower + "/" + FileNameConstructor.kw_type_lower +
                FileNameConstructor.kw_type_UPPER + "/" + FileNameConstructor.kw_types_plural_lower + "//" +
                FileNameConstructor.kw_types_plural_UPPER + "/" + FileNameConstructor.kw_types_plural_UPPER + ".TyPEs_PLURAL";
        fileNameConstructor.setTemplate(template);
        fileNameConstructor.afterPropertiesSet();
        for (UserObject userObject : list) {
            String type = abbreviate(userObject.getType().toLowerCase()).replace(" ", "_");
            String typePlural = pluralaze(abbreviate(userObject.getType())).replace(" ", "_");
            String fileName = type.toLowerCase() + "/" + type.toLowerCase() +
                    type.toUpperCase() + "/" + typePlural.toLowerCase() + "//" +
                    typePlural.toUpperCase() + "/" + typePlural.toUpperCase() + ".TyPEs_PLURAL";
            assertEquals(fileName, fileNameConstructor.map2FileName(userObject));
        }
    }

    @Test
    public void testApplyExtensionRules() {
        Map<String, String> extensionMap = new HashMap<String, String>();
        extensionMap.put("DEFAULT", "sql");
        extensionMap.put("VIEW", "vw");
        fileNameConstructor.setExtensionMap(extensionMap);
        UserObject userObject = new UserObject();
        userObject.setName("view01");
        userObject.setType("view");
        userObject.setSchema("");
        String fileName = pluralaze(abbreviate(userObject.getType())).replace(" ", "_") + "/" + userObject.getName() + ".vw";
        assertEquals(fileName, fileNameConstructor.map2FileName(userObject));
    }

    @Test(expected=IllegalStateException.class)
    public void testExtensionRulesWrongConfig() {
        Map<String, String> extensionMap = new HashMap<String, String>();
        extensionMap.put("VIEW", "vw");
        fileNameConstructor.setExtensionMap(extensionMap);
        UserObject userObject = new UserObject();
        userObject.setName("");
        userObject.setType("strange type");
        userObject.setSchema("");
        fileNameConstructor.map2FileName(userObject);
    }


}
