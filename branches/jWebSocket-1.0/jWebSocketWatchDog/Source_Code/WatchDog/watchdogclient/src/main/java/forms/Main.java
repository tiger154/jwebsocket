/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Main.java
 *
 * Created on Nov 21, 2011, 9:04:47 AM
 */
package forms;

import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javolution.util.FastList;
import org.jwebsocket.watchdog.api.ITestManager;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTest;
import org.jwebsocket.watchdog.test.TestManager;
import org.jwebsocket.watchdog.test.WatchDogTask;
import org.jwebsocket.watchdog.test.WatchDogTaskService;
import org.jwebsocket.watchdog.test.WatchDogTest;
import org.jwebsocket.watchdog.test.WatchDogTestService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author lester
 */
public class Main extends javax.swing.JFrame {

    WatchDogTestService mTestService;
    WatchDogTaskService mTaskService;
    ITestManager mTestManager;
    DefaultTableModel modelTest;
    DefaultTableModel modelTask;
    DefaultTableModel modelTaskTest;

    /** Creates new form Main */
    public Main() throws UnknownHostException, Exception {
        
        ApplicationContext ctx = new FileSystemXmlApplicationContext("conf/config.xml");
        mTestService = (WatchDogTestService)ctx.getBean("TestService");
        mTaskService = (WatchDogTaskService)ctx.getBean("TaskService");
        mTestManager = (TestManager)ctx.getBean("TestManager");
        
        initComponents();
        loadTestTable();
        loadTaskTable();
    }

    //Creating the structure of the table
    private void initTableTest() {
        //Setting the name of the columns of the Test table
        String[] columnsnamestest = new String[]{"id", "Description", "Implementation Class", "Fatal"};
        modelTest = new DefaultTableModel(columnsnamestest, 0);
        jTable1.setModel(modelTest);
        jTable1.setCellSelectionEnabled(true);
    }

    public void initTableTask() {
        //Setting the name of the columns of the Task Daily table
        String[] columnsnamesTask = new String[]{"id", "LastExecution", "Frecuency"};
        modelTask = new DefaultTableModel(columnsnamesTask, 0);
        jTable2.setModel(modelTask);
        jTable2.setCellSelectionEnabled(true);
    }

    public void initTableTaskTest() {
        //Setting the name of the columns of the Task Daily table
        String[] columnsnamestest = new String[]{"id", "Description", "Implementation Class", "Fatal"};
        modelTaskTest = new DefaultTableModel(columnsnamestest, 0);
        jTable4.setModel(modelTaskTest);
        jTable4.setCellSelectionEnabled(true);
    }

    private void loadTestTable() {

        initTableTest();
        List<WatchDogTest> list = mTestService.list();

        for (WatchDogTest i : list) {
            modelTest.addRow(new String[]{i.getId().toString(), i.getDescription().toString(), i.getImplClass().toString(), i.isFatal().toString()});
        }

    }

    private void loadTaskTable() {

        initTableTask();
        List<IWatchDogTask> list = mTaskService.list();
        System.out.println(">>>>" + list);

        for (IWatchDogTask i : list) {
            modelTask.addRow(new String[]{i.getId().toString(), i.getLastExecution(), i.getFrequency().toString()});
        }
    }

    private void loadTaskTestTable() {

        initTableTaskTest();
        List<IWatchDogTask> lTaskList = mTaskService.list();
        IWatchDogTask lTask = lTaskList.get(jTable2.getSelectedRow());

//        List<WatchDogTest> listr = new FastList<WatchDogTest>();
//        System.out.println(">>> ids " + ids);
//        System.out.println("list>> " + lTaskList);
//        listr = listTest(ids);
//
//        for (WatchDogTest i : listr) {
//            modelTaskTest.addRow(new String[]{i.getId().toString(), i.getDescription().toString(), i.getImplClass().toString(), i.isFatal().toString()});
//        }

        for (IWatchDogTest lTest : lTask.getTests()) {
            modelTaskTest.addRow(new String[]{lTest.getId().toString(), lTest.getDescription().toString(), lTest.getImplClass().toString(), lTest.isFatal().toString()});
        }
    }

    public List<WatchDogTest> listTest(List<String> id) {
        List<WatchDogTest> list = mTestService.list();
        List<WatchDogTest> listr = new FastList<WatchDogTest>();

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < id.size(); j++) {
                if (list.get(i).getId().equals(id.get(j))) {
                    listr.add(list.get(i));
                }
            }
        }
        return listr;
    }

    public Boolean searchTestDependency(String idTest) {
        Boolean flag = false;
        List<IWatchDogTask> lTastList = mTaskService.list();
        List<IWatchDogTest> lTests = null;
        for (int i = 0; i < lTastList.size(); i++) {
            lTests = lTastList.get(i).getTests();
            for (int j = 0; j < lTests.size(); j++) {
                if (lTests.get(j).getId().equals(idTest)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public Boolean searchIfIsTheLastTest(String idTest) {
        Boolean flag = false;
        List<IWatchDogTask> lTaskList = mTaskService.list();
        List<IWatchDogTest> lTests = null;
        for (int i = 0; i < lTaskList.size(); i++) {
            lTests = lTaskList.get(i).getTests();
            for (int j = 0; j < lTests.size(); j++) {
                if (lTests.get(j).getId().equals(idTest) && (lTests.size() == 1)) {
                    flag = true;
                }
            }
        }
        return flag;
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
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Task"));

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "Description", "Implementation Class", "Fatal"
            }
        ));
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
                .addContainerGap(44, Short.MAX_VALUE))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        jTable1.setColumnSelectionAllowed(true);
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

    //Removing a Test
    List<WatchDogTest> list = mTestService.list();

    try {
        if (jTable1.getSelectedRow() == -1) {
            throw new Exception("You should select a Test to remove");
        }
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected Task", "Removing Task...", 2);
        if (result == 0) {
            if (searchTestDependency(list.get(jTable1.getSelectedRow()).getId())) {

                int exist = JOptionPane.showConfirmDialog(this, "This Test is part of one Task. Do you want remove it anyway?", "Removing Test...", 2);
                if ((exist == 0) && (searchIfIsTheLastTest(list.get(jTable1.getSelectedRow()).getId()))) {

                    int isTheLast = JOptionPane.showConfirmDialog(this, "This is the las Test of the Task. If you remove this Test, the Task, will be removed also. Do you want remove it anyway?", "Removing Test...", 2);
                    if (isTheLast == 0) {
                        mTestService.remove(list.get(jTable1.getSelectedRow()).getId().toString());

                        loadTestTable();
                    }
                }
                mTestService.remove(list.get(jTable1.getSelectedRow()).getId().toString());
                loadTestTable();
            } else {
                mTestService.remove(list.get(jTable1.getSelectedRow()).getId().toString());
                loadTestTable();
            }
        }
        loadTestTable();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage());
    }

}//GEN-LAST:event_jButton6ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    try {
        //Adding a Watchdog Task
        AddTask tt = new AddTask(this, true, mTestService, true);
        tt.wtask = new WatchDogTask();
        tt.show(true);

        if (tt.wtask != null) {
            try {
                System.out.println(tt.wtask.asDocument().toString());

                mTaskService.add(tt.wtask);
                loadTaskTable();
                JOptionPane.showMessageDialog(this, "The Task " + tt.wtask.getId().toString() + " have been inserted SUCCESFULY");
                //System.out.println(tt.wtask.getId());
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
    AddTest tt = new AddTest(this, true, true);
    tt.wt = new WatchDogTest();
    tt.show(true);

    if (tt.wt != null) {
        try {
            mTestService.add(tt.wt);
            loadTestTable();
            JOptionPane.showMessageDialog(this, "The test " + tt.wt.getId().toString() + " have been inserted SUCCESFULY");
            //System.out.println(tt.wtask.getId());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

}//GEN-LAST:event_jMenu2ActionPerformed

private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

    //Adding a WatchDog Test
    AddTest tt = new AddTest(this, true, true);
    tt.wt = new WatchDogTest();
    tt.show(true);

    if (tt.wt != null) {
        try {
            mTestService.add(tt.wt);
            loadTestTable();
            JOptionPane.showMessageDialog(this, "The test " + tt.wt.getId().toString() + " have been inserted SUCCESFULY");
            //System.out.println(tt.wtask.getId());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}//GEN-LAST:event_jMenuItem1ActionPerformed

private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
    loadTaskTestTable();
}//GEN-LAST:event_jTable2MouseClicked

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    //removing a TASK...

    List<IWatchDogTask> list = mTaskService.list();
    System.out.println(">>> IwatchDog " + list);

    try {
        if (jTable2.getSelectedRow() == -1) {
            throw new Exception("You should select a Task to remove");
        }
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected Task", "Removing Task...", 2);
        if (result == 0) {
            mTaskService.remove(list.get(jTable2.getSelectedRow()).getId().toString());
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

        AddTask tt = new AddTask(this, true, mTestService, false);
        tt.wtask = new WatchDogTask();
        tt.show(true);
        
        int row = jTable2.getSelectedRow();

        tt.wtask.setId(modelTest.getValueAt(row, 1).toString());
       /* tt.wtask.set(modelTest.getValueAt(row, 1).toString());
        tt.wtask.setImplClass(modelTest.getValueAt(row, 2).toString());
        tt.wtask.setIsFatal(Boolean.valueOf(modelTest.getValueAt(row, 3).toString()));
      */


       // tt.show(true);

        if (tt.wtask != null) {
            try {
                mTaskService.modify(tt.wtask.getId(), tt.wtask);
                //wtests.add(tt.wt);
                loadTestTable();
                JOptionPane.showMessageDialog(this, "The test " + tt.wtask.getId().toString() + " have been updated SUCCESFULY");
                //System.out.println(tt.wtask.getId());
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
    AddTest tt = new AddTest(this, true, true);
    tt.wt = new WatchDogTest();

    tt.show(true);

    if (tt.wt != null) {
        try {
            mTestService.add(tt.wt);
            loadTestTable();
            JOptionPane.showMessageDialog(this, "The test " + tt.wt.getId().toString() + " have been inserted SUCCESFULY");
            //System.out.println(tt.wtask.getId());
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

        AddTest tt = new AddTest(this, true, false);
        tt.wt = new WatchDogTest();
        int row = jTable1.getSelectedRow();

        tt.wt.setId(modelTest.getValueAt(row, 0).toString());
        tt.wt.setDescription(modelTest.getValueAt(row, 1).toString());
        tt.wt.setImplClass(modelTest.getValueAt(row, 2).toString());
        tt.wt.setIsFatal(Boolean.valueOf(modelTest.getValueAt(row, 3).toString()));



        tt.show(true);

        if (tt.wt != null) {
            try {
                mTestService.modify(tt.wt.getId(), tt.wt);
                //wtests.add(tt.wt);
                loadTestTable();
                JOptionPane.showMessageDialog(this, "The test " + tt.wt.getId().toString() + " have been updated SUCCESFULY");
                //System.out.println(tt.wtask.getId());
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
    List<IWatchDogTask> list = mTaskService.list();
    IWatchDogTask lTask = list.get(jTable2.getSelectedRow());
    //mTestManager.execute(lTask);
    
    Results tt = new Results(this, true, mTestManager.execute(lTask));
    tt.show(true);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
    
  
}//GEN-LAST:event_jButton4ActionPerformed

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
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
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
