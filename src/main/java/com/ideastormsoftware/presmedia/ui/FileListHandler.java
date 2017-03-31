/*
 * Copyright 2017 Phillip Hayward
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ideastormsoftware.presmedia.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

class FileListHandler extends TransferHandler {

    private final DefaultListModel<File> model;

    FileListHandler(DefaultListModel<File> model) {
        this.model = model;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }

        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable transferable = support.getTransferable();
        List<File> files;

        try {
            files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            return false;
        }

        JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
        int index = dropLocation.getIndex();

        for (int i = files.size()-1; i >=0; i--) {
            model.add(index, files.get(i));
        }
        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (!(c instanceof JList))
            return null;
        return new FileListTransferable(((JList<File>)c).getSelectedValuesList());
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        JList<File> mediaList = (JList<File>) source;
        List<Integer> indices = new ArrayList<>();
        for (int index : mediaList.getSelectedIndices()) {
            indices.add(index);
        }
        Collections.sort(indices);
        for (int i = indices.size()-1; i >=0; i--) {
            model.removeElementAt(indices.get(i));
        }
        
        System.out.println("export done");
    }

    private static class FileListTransferable implements Transferable {

        private final List<File> data;

        public FileListTransferable(List<File> data) {
            this.data = data;
        }

        DataFlavor[] supportedFlavors = {DataFlavor.javaFileListFlavor};

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return Arrays.copyOf(supportedFlavors, supportedFlavors.length);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return data;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

}
