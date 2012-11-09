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

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Reshetnikov AV resheto@gmail.com
 * Date: 20.02.11
 * Time: 18:21
 */
public class Worker {

    private Dao dao;
    private FileWorker fileWorker;
    private DDLFormatter ddlFormatter;


    public void work() {
        System.out.println("start getting of user object list for processing");
        List<UserObject> list = dao.getUserObjectList();
        System.out.println("get " + list.size() + " objects");
        for (UserObject obj : list){
            obj = dao.fillDDL(obj);
            ddlFormatter.formatDDL(obj);
            fileWorker.save2file(obj);
           // System.out.print(".");
        }
        System.out.println(" done " );
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    public void setFileWorker(FileWorker fileWorker) {
        this.fileWorker = fileWorker;
    }

    public Dao getDao() {
        return dao;
    }

    public FileWorker getFileWorker() {
        return fileWorker;
    }

    public void setDdlFormatter(DDLFormatter ddlFormatter) {
        this.ddlFormatter = ddlFormatter;
    }
}
