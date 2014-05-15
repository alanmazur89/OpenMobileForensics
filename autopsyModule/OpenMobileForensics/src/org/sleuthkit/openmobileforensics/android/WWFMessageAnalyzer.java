/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.openmobileforensics.android;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

 class WWFMessageAnalyzer {
    private Connection connection = null;
    private ResultSet resultSet = null;
    private Statement statement = null;
    private String dbPath = "";
    private long fileId = 0;
    private java.io.File jFile = null;
    private String moduleName= AndroidModuleFactory.getModuleName();
    private static final Logger logger = Logger.getLogger(WWFMessageAnalyzer.class.getName());
    public void findWWFMessages() {
        List<AbstractFile> absFiles;
        try {
            SleuthkitCase skCase = Case.getCurrentCase().getSleuthkitCase();
            absFiles = skCase.findAllFilesWhere("name ='WordsFramework' "); //get exact file names
            if (absFiles.isEmpty()) {
                return;
            }
            for (AbstractFile AF : absFiles) {
                try {
                    jFile = new java.io.File(Case.getCurrentCase().getTempDirectory(), AF.getName());
                    ContentUtils.writeToFile(AF,jFile);
                    dbPath = jFile.toString(); //path of file as string
                    fileId = AF.getId();
                    findWWFMessagesInDB(dbPath, fileId);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error parsing WWF messages", e);
                }
            }
        } catch (TskCoreException e) {
            logger.log(Level.SEVERE, "Error finding WWF messages", e);
        }
    }
     private void findWWFMessagesInDB(String DatabasePath, long fId) {
        if (DatabasePath == null || DatabasePath.isEmpty()) {
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC"); //load JDBC driver
            connection = DriverManager.getConnection("jdbc:sqlite:" + DatabasePath);
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
             logger.log(Level.SEVERE, "Error opening database", e);
        }

        Case currentCase = Case.getCurrentCase();
        SleuthkitCase skCase = currentCase.getSleuthkitCase();
        try {
            AbstractFile f = skCase.getAbstractFileById(fId);
            try {
                resultSet = statement.executeQuery(
                        "SELECT message,created_at,user_id,game_id FROM chat_messages ORDER BY game_id DESC, created_at DESC;");

                BlackboardArtifact bba;
                String message; // WWF Message
                String created_at; // unix time
                String user_id; // the ID of the user who sent the message.
                String game_id; // ID of the game which the the message was sent.
              

                while (resultSet.next()) {
                    message = resultSet.getString("message");
                    created_at = resultSet.getString("created_at");
                    user_id = resultSet.getString("user_id");
                    game_id = resultSet.getString("game_id");

                    bba = f.newArtifact(BlackboardArtifact.ARTIFACT_TYPE.TSK_MESSAGE); //create a call log and then add attributes from result set.
                    bba.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME.getTypeID(), moduleName, created_at));
                    bba.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME.getTypeID(), moduleName, user_id));
                    bba.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_MSG_ID.getTypeID(), moduleName, game_id));
                    bba.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_TEXT.getTypeID(), moduleName,message));
                    bba.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_MESSAGE_TYPE.getTypeID(), moduleName,"Words With Friends Message" ));

                }


            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error parsing WWF messages to the Blackboard", e);
            } finally {
                try {
                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (Exception e) {
                     logger.log(Level.SEVERE, "Error closing database", e);
                }
            }
        } catch (Exception e) {
             logger.log(Level.SEVERE, "Error parsing WWF messages to the Blackboard", e);
        }

    }


}
