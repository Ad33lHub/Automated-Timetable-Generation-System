package gui;

import database.SubjectAssignmentDAO;
import database.TeacherDAO;
import database.SubjectDAO;
import models.*;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SubjectAssignmentPanel extends JPanel {
    private final SubjectAssignmentDAO dao = new SubjectAssignmentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;

    public SubjectAssignmentPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(StyleConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Faculty Allocations");
        title.setFont(StyleConstants.FONT_TITLE);
        title.setForeground(StyleConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actionGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGrp.setOpaque(false);
        JButton addBtn = createActionButton("+ Create Assignment", StyleConstants.SUCCESS);
        JButton delBtn = createActionButton("Remove", StyleConstants.DANGER);
        actionGrp.add(addBtn); actionGrp.add(delBtn);
        header.add(actionGrp, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Teacher Name", "Allocated Subject"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.putClientProperty(FlatClientProperties.STYLE, "arc: 12; border: 1,1,1,1," + StyleConstants.toHex(StyleConstants.BORDER));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(StyleConstants.BG_CARD);
        add(sp, BorderLayout.CENTER);

        // Actions
        addBtn.addActionListener(e -> showForm());
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) return;
            if (JOptionPane.showConfirmDialog(this, "Deallocate this subject?") == 0) {
                dao.delete((int)tableModel.getValueAt(r, 0));
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (SubjectAssignment sa : dao.getAll()) 
            tableModel.addRow(new Object[]{sa.getId(), sa.getTeacherName(), sa.getSubjectName()});
    }

    private void showForm() {
        List<Teacher> teachers = teacherDAO.getAll();
        List<Subject> subjects = subjectDAO.getAll();

        if (teachers.isEmpty() || subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Requires teachers and subjects records.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel p = new JPanel(new GridLayout(2, 1, 15, 15));
        p.setBackground(StyleConstants.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<Teacher> teacherCB = new JComboBox<>(teachers.toArray(new Teacher[0]));
        JComboBox<Subject> subjectCB = new JComboBox<>(subjects.toArray(new Subject[0]));
        teacherCB.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        subjectCB.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        p.add(layoutCombo("Select Faculty", teacherCB));
        p.add(layoutCombo("Select Subject", subjectCB));

        int res = JOptionPane.showConfirmDialog(this, p, "Subject Allocation", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (res == 0) {
            SubjectAssignment sa = new SubjectAssignment();
            sa.setTeacherId(((Teacher)teacherCB.getSelectedItem()).getId());
            sa.setSubjectId(((Subject)subjectCB.getSelectedItem()).getId());
            dao.insert(sa);
            refresh();
        }
    }

    private JPanel layoutCombo(String label, JComboBox<?> cb) {
        JPanel p = new JPanel(new BorderLayout(5, 5)); p.setOpaque(false);
        p.add(StyleConstants.createSecondaryLabel(label.toUpperCase()), BorderLayout.NORTH);
        p.add(cb, BorderLayout.CENTER);
        return p;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(StyleConstants.FONT_SMALL); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 8,15,8,15");
        return b;
    }
}
