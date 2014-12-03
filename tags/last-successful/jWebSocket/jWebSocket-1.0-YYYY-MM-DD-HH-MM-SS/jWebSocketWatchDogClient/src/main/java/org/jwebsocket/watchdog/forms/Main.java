// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-212 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.watchdog.forms;

import com.mongodb.MongoException;
import java.awt.Graphics;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javolution.util.FastList;
import org.jwebsocket.watchdog.api.ITestManager;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTest;
import org.jwebsocket.watchdog.executor.TaskExecutor;
import org.jwebsocket.watchdog.listener.WatchDogTestListener;
import org.jwebsocket.watchdog.notifier.GlobsNotifier;
import org.jwebsocket.watchdog.test.TestManager;
import org.jwebsocket.watchdog.test.WatchDogTask;
import org.jwebsocket.watchdog.test.WatchDogTaskService;
import org.jwebsocket.watchdog.test.WatchDogTest;
import org.jwebsocket.watchdog.test.WatchDogTestService;
import org.jwebsocket.watchdog.test.TaskExecutionReport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class Main extends javax.swing.JFrame {

    WatchDogTestService mTestService;
    WatchDogTaskService mTaskService;
    ITestManager mTestManager;
    DefaultTableModel mModelTest;
    DefaultTableModel mModelTask;
    DefaultTableModel mModelTaskTest;
    TaskExecutor mTaskExecutor;
    TaskExecutionReport mTaskExecutorReport;
    WatchDogTestListener mListener;

    /** Creates new form Main */
    public Main() throws UnknownHostException, Exception {

        initComponents();
        this.setTitle("jWebSocket WatchDog Client");
        this.setIconImage(new ImageIcon("conf/icon.png").getImage());
        this.setLocation(180, 140);
        jLabel1.setIcon(new ImageIcon("conf/Enapso32x32.png"));

        GlobsNotifier gn = new GlobsNotifier(this);


        ApplicationContext ctx = new FileSystemXmlApplicationContext(
                "conf/config.xml");

        mTestService = (WatchDogTestService) ctx.getBean("TestService");
        mTaskService = (WatchDogTaskService) ctx.getBean("TaskService");
        mTestManager = (TestManager) ctx.getBean("TestManager");

        mTaskExecutor = new TaskExecutor(mTestManager, mTaskService);
        mTaskExecutor.start();

        try {
            loadTestTable();
            loadTaskTable();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

//Creating the structure of the table
    private void initTableTest() {
        //Setting the name of the columns of the Test table
        String[] lColumnsnamestest = new String[]{"id",
            "Description", "Implementation Class", "Fatal"};
        mModelTest = new DefaultTableModel(lColumnsnamestest, 0);
        //jTable1.setRowSelectionInterval(0, 0);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable1.setColumnSelectionAllowed(false);
        jTable1.setRowSelectionAllowed(true);
        jTable1.setModel(mModelTest);
    }

    public void initTableTask() {
        //Setting the name of the columns of the Task Daily table
        String[] lColumnsnamesTask = new String[]{"id", "LastExecution", "Frecuency"};
        mModelTask = new DefaultTableModel(lColumnsnamesTask, 0);
        jTable2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable2.setColumnSelectionAllowed(false);
        jTable2.setRowSelectionAllowed(true);
        jTable2.setModel(mModelTask);
    }

    public void initTableTaskTest() {
        //Setting the name of the columns of the Task Daily table
        String[] lColumnsnamestest = new String[]{"id",
            "Description", "Implementation Class", "Fatal"};
        mModelTaskTest = new DefaultTableModel(lColumnsnamestest, 0);
        jTable4.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable4.setColumnSelectionAllowed(false);
        jTable4.setRowSelectionAllowed(true);
        jTable4.setModel(mModelTaskTest);
    }

    private void loadTestTable() {

        initTableTest();
        try {
            List<WatchDogTest> lList = mTestService.list();
            String lFatal = "No";
            for (WatchDogTest i : lList) {
                if (i.isFatal()) {
                    lFatal = "Yes";
                }
                mModelTest.addRow(new String[]{i.getId().toString(),
                            i.getDescription().toString(), i.getImplClass().toString(), lFatal});
                lFatal = "No";
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadTaskTable() {

        initTableTask();
        List<IWatchDogTask> lList = mTaskService.list();

        String lType = "";
        for (IWatchDogTask i : lList) {
            if (i.getType().equals("m")) {
                if (i.getEveryNMinutes() == 1) {
                    lType = "Every " + i.getEveryNMinutes() + " Minute";
                } else {
                    lType = "Every " + i.getEveryNMinutes() + " Minutes";
                }
            }
            if (i.getType().equals("h")) {
                if (i.getEveryNHours() == 1) {
                    lType = "Every " + i.getEveryNHours() + " Hour";
                } else {
                    lType = "Every " + i.getEveryNHours() + " Hours";
                }
            }
            if (i.getType().equals("d")) {
                if (i.getEveryNDays() == 1) {
                    lType = "Every " + i.getEveryNDays() + " Day";
                } else {
                    lType = "Every " + i.getEveryNDays() + " Days";
                }
            }
            mModelTask.addRow(new String[]{i.getId().toString(),
                        i.getLastExecution(), lType});
            lType = "";
        }
    }

    private void loadTaskTestTable() {

        initTableTaskTest();
        List<IWatchDogTask> lTaskList = mTaskService.list();
        IWatchDogTask lTask = lTaskList.get(jTable2.getSelectedRow());

        String lFatal = "No";
        if (!lTaskList.isEmpty()) {

            for (IWatchDogTest lTest : lTask.getTests()) {
                if (lTest.isFatal()) {
                    lFatal = "Yes";
                }
                mModelTaskTest.addRow(new String[]{lTest.getId().toString(),
                            lTest.getDescription().toString(), lTest.getImplClass().toString(),
                            lFatal.toString()});
                lFatal = "No";
            }
        }
    }

    public List<WatchDogTest> listTest(List<String> id) throws Exception {

        List<WatchDogTest> lList = mTestService.list();
        List<WatchDogTest> lListr = new FastList<WatchDogTest>();

        for (int i = 0; i < lList.size(); i++) {
            for (int j = 0; j < id.size(); j++) {
                if (lList.get(i).getId().equals(id.get(j))) {
                    lListr.add(lList.get(i));
                }
            }
        }
        return lListr;
    }

    public Boolean searchTestDependency(String aIdTest) {
        Boolean lFlag = false;
        List<IWatchDogTask> lTastList = mTaskService.list();
        List<IWatchDogTest> lTests = null;
        for (int i = 0; i < lTastList.size(); i++) {
            lTests = lTastList.get(i).getTests();
            for (int j = 0; j < lTests.size(); j++) {
                if (lTests.get(j).getId().equals(aIdTest)) {
                    lFlag = true;
                }
            }
        }
        return lFlag;
    }

    public Boolean searchIfIsTheLastTest(String aIdTest) {
        Boolean lFlag = false;
        List<IWatchDogTask> lTaskList = mTaskService.list();
        List<IWatchDogTest> lTests = null;
        for (int i = 0; i < lTaskList.size(); i++) {
            lTests = lTaskList.get(i).getTests();
            for (int j = 0; j < lTests.size(); j++) {
                if (lTests.get(j).getId().equals(aIdTest) && (lTests.size() == 1)) {
                    lFlag = true;
                }
            }
        }
        return lFlag;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Task"));

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "Description", "Implementation Class", "Fatal"
            }
        ));
        jTable4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable4MouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(jTable4);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable2);

        jButton1.setText("Add Task");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Remove Task");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Update Task");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Execute Task");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton3)
                    .addComponent(jButton2)
                    .addComponent(jButton4))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Test"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jButton6.setText("Remove Test");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("Add Test");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Update Test");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton8)
                    .addComponent(jButton6))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel1.setText("jWebSocket Watchdog Client v1.0");

        jMenu1.setText("File");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem2.setText("Exit");
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setText("Add Test");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem3.setText("Add Task");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

    //Removing a Test
    try {
        List<WatchDogTest> lList = mTestService.list();
        if (jTable1.getSelectedRow() == -1) {
            throw new Exception("You should select a Test to remove");
        }
        int lResult = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected Test", "Removing Test...", 2);
        if (lResult == 0) {
            mTestService.remove(lList.get(jTable1.getSelectedRow()).getId().toString());
            loadTestTable();
        }
        loadTestTable();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage());
    }

}//GEN-LAST:event_jButton6ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    try {
        //Adding a Watchdog Task
        AddTask lAddTask = new AddTask(this, true, mTestService, true);
        lAddTask.mWatchDogTask = new WatchDogTask();
        lAddTask.show(true);

        if (lAddTask.mWatchDogTask != null) {
            try {
                System.out.println(lAddTask.mWatchDogTask.asDocument().toString());

                mTaskService.add(lAddTask.mWatchDogTask);
                loadTaskTable();

            } catch (MongoException ex) {
                JOptionPane.showMessageDialog(this, "Mongo Exception");
            }
        }
    } catch (UnknownHostException ex) {
        JOptionPane.showMessageDialog(this, "Unknown Host Exception");
    }
}//GEN-LAST:event_jButton1ActionPerformed

private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed

    //Adding a WatchDog Test
    AddTest lAddTest = new AddTest(this, true, true);
    lAddTest.mWatchDogTest = new WatchDogTest();
    lAddTest.show(true);

    if (lAddTest.mWatchDogTest != null) {
        try {
            mTestService.add(lAddTest.mWatchDogTest);
            loadTestTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

}//GEN-LAST:event_jMenu2ActionPerformed

private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

    //Adding a WatchDog Test
    AddTest lAddTest = new AddTest(this, true, true);
    lAddTest.mWatchDogTest = new WatchDogTest();
    lAddTest.show(true);

    if (lAddTest.mWatchDogTest != null) {
        try {
            mTestService.add(lAddTest.mWatchDogTest);
            loadTestTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}//GEN-LAST:event_jMenuItem1ActionPerformed

private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
    loadTaskTestTable();
    loadTestTable();
}//GEN-LAST:event_jTable2MouseClicked

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    //removing a TASK...
    List<IWatchDogTask> lList = mTaskService.list();

    try {
        if (jTable2.getSelectedRow() == -1) {
            throw new Exception("You should select a Task to remove");
        }
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected Task", "Removing Task...", 2);
        if (result == 0) {
            mTaskService.remove(lList.get(jTable2.getSelectedRow()).getId().toString());
        }
        loadTaskTable();
    } catch (Exception ex) {

        JOptionPane.showMessageDialog(this, ex.getMessage());

    }
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

    //Update Task
    try {
        if (jTable2.getSelectedRow() == -1) {
            throw new Exception("You should select a Task to update");
        }

        if (jTable2.getSelectedRows().length > 1) {
            throw new Exception("You can't select more than one Task to update");
        }
        int lRow = jTable2.getSelectedRow();
        WatchDogTask lWatchDogTask = new WatchDogTask();
        String lOldId = mModelTask.getValueAt(lRow, 0).toString();
        lWatchDogTask.setId(mModelTask.getValueAt(lRow, 0).toString());


        AddTask lAddTask = new AddTask(this, true, lWatchDogTask, mTestService, false);

        lAddTask.show(true);

        if (lAddTask.mWatchDogTask != null) {
            try {

                mTaskService.modify(lOldId, lAddTask.mWatchDogTask);
                //wtests.add(tt.mWatchDogTest);
                loadTaskTable();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
//Adding a WatchDog Test
    AddTest lAddTest = new AddTest(this, true, true);
    lAddTest.mWatchDogTest = new WatchDogTest();

    lAddTest.show(true);

    if (lAddTest.mWatchDogTest != null) {
        try {
            mTestService.add(lAddTest.mWatchDogTest);
            loadTestTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}//GEN-LAST:event_jButton7ActionPerformed

private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

    //Updating a WatchDog Test
    try {
        if (jTable1.getSelectedRow() == -1) {
            throw new Exception("You should select a Test to update");
        }

        if (jTable1.getSelectedRows().length > 1) {
            throw new Exception("You can't select more than one Test to update");
        }

        AddTest lAddTest = new AddTest(this, true, false);
        lAddTest.mWatchDogTest = new WatchDogTest();

        int lRow = jTable1.getSelectedRow();

        lAddTest.mWatchDogTest.setId(mModelTest.getValueAt(lRow, 0).toString());
        lAddTest.mWatchDogTest.setDescription(mModelTest.getValueAt(lRow, 1).toString());
        lAddTest.mWatchDogTest.setImplClass(mModelTest.getValueAt(lRow, 2).toString());
        lAddTest.mWatchDogTest.setIsFatal(Boolean.valueOf("Yes".equals(mModelTest.getValueAt(lRow, 3).toString()) ? "true" : "false"));

        lAddTest.show(true);

        if (lAddTest.mWatchDogTest != null) {
            try {
                mTestService.modify(lAddTest.mWatchDogTest.getId(), lAddTest.mWatchDogTest);
                loadTestTable();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }

        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }

}//GEN-LAST:event_jButton8ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

    //Showing test results
    try {
        if (jTable2.getSelectedRow() == -1) {
            throw new Exception("You should select a task to execute Manually");
        }
        List<IWatchDogTask> lList = mTaskService.list();
        IWatchDogTask lTask = lList.get(jTable2.getSelectedRow());

        Results lResultWindow = new Results(this, true, mTestManager.execute(lTask));
        mTaskService.updateLastExecution(lTask, new Date());

        loadTaskTable();
        lResultWindow.show(true);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}//GEN-LAST:event_jButton4ActionPerformed

private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
    try {
        //Adding a Watchdog Task
        AddTask lAddTask = new AddTask(this, true, mTestService, true);
        lAddTask.mWatchDogTask = new WatchDogTask();
        lAddTask.show(true);

        if (lAddTask.mWatchDogTask != null) {
            try {
                System.out.println(lAddTask.mWatchDogTask.asDocument().toString());

                mTaskService.add(lAddTask.mWatchDogTask);
                loadTaskTable();

            } catch (MongoException ex) {
                JOptionPane.showMessageDialog(this, "Mongo Exception");
            }
        }
    } catch (UnknownHostException ex) {
        JOptionPane.showMessageDialog(this, "Unknown Host Exception");
    }
}//GEN-LAST:event_jMenuItem3ActionPerformed

private void jTable4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable4MouseClicked
    loadTaskTable();
    loadTestTable();

}//GEN-LAST:event_jTable4MouseClicked

private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
    loadTaskTable();
}//GEN-LAST:event_jTable1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(WatchDogTest args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;


                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    new Main().setVisible(true);


                } catch (UnknownHostException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable4;
    // End of variables declaration//GEN-END:variables
}
