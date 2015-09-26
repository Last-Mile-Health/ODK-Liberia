/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.lastmilehealth.collect.android.utilities;

import android.util.Log;
import org.apache.commons.io.IOUtils;
import org.lastmilehealth.collect.android.application.Collect;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
    final static String TAG = "~";
    private static final int BUFFER = 1024;

    public static void unzip(File[] zipFiles) {
        for (File zipFile : zipFiles) {
            ZipInputStream zipInputStream = null;
            try {
                zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    doExtractInTheSameFolder(zipFile, zipInputStream, zipEntry);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(zipInputStream);
            }
        }
    }

    public static File extractFirstZipEntry(File zipFile, boolean deleteAfterUnzip) throws IOException {
        ZipInputStream zipInputStream = null;
        File targetFile = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            if (zipEntry != null) {
                targetFile = doExtractInTheSameFolder(zipFile, zipInputStream, zipEntry);
            }
        } finally {
            IOUtils.closeQuietly(zipInputStream);
        }

        if (deleteAfterUnzip && targetFile != null && targetFile.exists()) {
            FileUtils.deleteAndReport(zipFile);
        }

        return targetFile;
    }

    private static File doExtractInTheSameFolder(File zipFile, ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
        File targetFile;
        String fileName = zipEntry.getName();

        Log.w(TAG, "Found zipEntry with name: " + fileName);

        if (fileName.contains("/") || fileName.contains("\\")) {
            // that means that this is a directory of a file inside a directory, so ignore it
            Log.w(TAG, "Ignored: " + fileName);
            return null;
        }

        // extract the new file
        targetFile = new File(zipFile.getParentFile(), fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(targetFile);
            IOUtils.copy(zipInputStream, fileOutputStream);
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }

        Log.w(TAG, "Extracted file \"" + fileName + "\" out of " + zipFile.getName());
        return targetFile;
    }

    public static void zip(String[] _files, String zipFileName) {
        try {
            File destination = new File(zipFileName);
            if(destination.exists())
                destination.delete();

            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                if (new File(_files[i]).exists()) {
                    //Log.d(TAG, "Adding to " + zipFileName+ ": " + _files[i]);
                    FileInputStream fi = new FileInputStream(_files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);

                    ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;

                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void zipInstances(String zipFileName) throws Exception {
        //delete previous zip
        org.apache.commons.io.FileUtils.deleteQuietly(new File(zipFileName));

        File dirObj = new File(Collect.INSTANCES_PATH);
        if(dirObj.isDirectory()) {
            if(dirObj.list().length == 0)
                throw new Exception("No instance files");
        }

        //copy db file to archive it too
        org.apache.commons.io.FileUtils.copyFile(new File(Collect.INSTANCES_DB_PATH), new File(Collect.INSTANCES_DB_TMP_PATH));

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        addInstancesToZip(dirObj, out);

        //delete db file
        org.apache.commons.io.FileUtils.deleteQuietly(new File(Collect.INSTANCES_DB_TMP_PATH));

        out.close();
    }

    public static void zipForms(String zipFileName) throws Exception {
        //delete previous zip
        org.apache.commons.io.FileUtils.deleteQuietly(new File(zipFileName));

        File dirObj = new File(Collect.FORMS_PATH);
        if(dirObj.isDirectory()) {
            if(dirObj.list().length == 0)
                throw new Exception("No forms files");
        }

        //create "form flag" file (so, receiver know this is empty forms and not instances)
        File formFlag = new File(Collect.FORMS_FLAG_PATH);
        try {
            formFlag.createNewFile();
        }
        catch (IOException e) {}

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        addFormsToZip(dirObj, out);

        //delete form flag file
        org.apache.commons.io.FileUtils.deleteQuietly(formFlag);

        out.close();
    }

    private static void addInstancesToZip(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addInstancesToZip(files[i], out);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            Log.d("~",  "Zipping instances: " + files[i].getAbsolutePath().substring(Collect.INSTANCES_PATH.length()+1));
            out.putNextEntry(new ZipEntry(files[i].getAbsolutePath().substring(Collect.INSTANCES_PATH.length()+1)));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }

            out.closeEntry();
            in.close();
        }
    }

    private static void addFormsToZip(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addFormsToZip(files[i], out);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            Log.d("~",  "Zipping forms: " + files[i].getAbsolutePath().substring(Collect.FORMS_PATH.length()+1));
            out.putNextEntry(new ZipEntry(files[i].getAbsolutePath().substring(Collect.FORMS_PATH.length()+1)));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }

            out.closeEntry();
            in.close();
        }
    }

    /**private static void addFile(File fileObj, ZipOutputStream out) throws IOException {
        //Log.d("~",  "Add file: "+fileObj.getName());
        out.putNextEntry(new ZipEntry(fileObj.getName()));
        out.closeEntry();
    }*/

    public static void unzip(String zipFile, String targetLocation) throws Exception {
        //delete target location to clear previously unzipped data
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(targetLocation));
        } catch (Exception e) {}
        createDirIfNotExist(targetLocation);

        FileInputStream fin = new FileInputStream(zipFile);
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry ze = null;
        while ((ze = zin.getNextEntry()) != null) {
            //create dir if required while unzipping
            if (ze.isDirectory()) {
                createDirIfNotExist(ze.getName());
            } else {
                String path = targetLocation + "/" + ze.getName();
                Log.d(TAG, "unzipping: "+path);

                if(path.contains("/")) {
                    org.apache.commons.io.FileUtils.forceMkdir(new File(path.substring(0, path.lastIndexOf("/"))));
                }

                FileOutputStream fout = new FileOutputStream(path);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                zin.closeEntry();
                fout.close();
            }
        }
        zin.close();
    }

    public static void createDirIfNotExist(String dirPath) {
        File directory = new File(dirPath);
        if(!directory.exists())
            directory.mkdir();
    }
}
