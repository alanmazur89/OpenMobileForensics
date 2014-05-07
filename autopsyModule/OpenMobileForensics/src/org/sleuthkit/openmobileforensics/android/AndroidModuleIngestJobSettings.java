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

import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

/**
 * Ingest job options for sample ingest module instances.
 */
public class AndroidModuleIngestJobSettings implements IngestModuleIngestJobSettings {
    
    private boolean skipKnownFiles = true;

    AndroidModuleIngestJobSettings() {
    }

    AndroidModuleIngestJobSettings(boolean skipKnownFiles) {
        this.skipKnownFiles = skipKnownFiles;
    }

    @Override
    public String getVersionNumber() {
        return "1.0"; //NON-NLS
    }    
    
    void setSkipKnownFiles(boolean enabled) {
        skipKnownFiles = enabled;
    }

    boolean skipKnownFiles() {
        return skipKnownFiles;
    }
}