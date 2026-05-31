package gui;

import database.SubjectDAO;
import models.Subject;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SubjectPanel extends JPanel {
    private final SubjectDAO dao = new SubjectDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;

    public SubjectPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(StyleConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Curriculum Management");
        title.setFont(StyleConstants.FONT_TITLE);
        title.setForeground(StyleConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actionGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGrp.setOpaque(false);
        JButton addBtn = createActionButton("+ Add Subject", StyleConstants.SUCCESS);
        JButton editBtn = createActionButton("Edit", StyleConstants.ACCENT);
        JButton delBtn = createActionButton("Delete", StyleConstants.DANGER);
        actionGrp.add(addBtn); actionGrp.add(editBtn); actionGrp.add(delBtn);
        header.add(actionGrp, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Code", "Name", "Hours", "Dept", "Section"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setGridColor(StyleConstants.BORDER);
        table.putClientProperty(FlatClientProperties.STYLE, "arc: 12; border: 1,1,1,1," + StyleConstants.toHex(StyleConstants.BORDER));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(StyleConstants.BG_CARD);
        add(sp, BorderLayout.CENTER);

        // Actions
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) return;
            showForm(new Subject(
                (int)tableModel.getValueAt(r, 0), 
                (String)tableModel.getValueAt(r, 2), 
                (String)tableModel.getValueAt(r, 1), 
                (int)tableModel.getValueAt(r, 3), 
                (String)tableModel.getValueAt(r, 4),
                (String)tableModel.getValueAt(r, 5)
            ));
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) return;
            if (JOptionPane.showConfirmDialog(this, "Remove this subject?") == 0) {
                dao.delete((int)tableModel.getValueAt(r, 0));
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (Subject s : dao.getAll()) 
            tableModel.addRow(new Object[]{s.getId(), s.getCode(), s.getName(), s.getCreditHours(), s.getDepartment(), s.getSection()});
    }

    private void showForm(Subject s) {
        JPanel p = new JPanel(new GridLayout(5, 1, 15, 15));
        p.setBackground(StyleConstants.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameF = new JTextField();
        JTextField codeF = new JTextField();
        JTextField chF = new JTextField();
        JTextField deptF = new JTextField();
        JTextField sectF = new JTextField();

        StyleConstants.applyFieldStyle(nameF, "Subject Name");
        StyleConstants.applyFieldStyle(codeF, "Code (e.g. CS101)");
        StyleConstants.applyFieldStyle(chF, "Credit Hours");
        StyleConstants.applyFieldStyle(deptF, "Department");
        StyleConstants.applyFieldStyle(sectF, "Section (e.g. SP26-1)");

        if (s != null) {
            nameF.setText(s.getName());
            codeF.setText(s.getCode());
            chF.setText(String.valueOf(s.getCreditHours()));
            deptF.setText(s.getDepartment());
            sectF.setText(s.getSection());
        }

        p.add(layoutField("Subject Name", nameF));
        p.add(layoutField("Code", codeF));
        p.add(layoutField("Credit Hours", chF));
        p.add(layoutField("Department", deptF));
        p.add(layoutField("Section", sectF));

        int res = JOptionPane.showConfirmDialog(this, p, s == null ? "Add Subject" : "Update Subject", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (res == 0) {
            String name = nameF.getText().trim();
            String code = codeF.getText().trim();
            String hoursStr = chF.getText().trim();
            String dept = deptF.getText().trim();
            String section = sectF.getText().trim();

            if (name.isEmpty() || code.isEmpty() || hoursStr.isEmpty() || section.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int hours = Integer.parseInt(hoursStr);
                Subject ns = s == null ? new Subject() : s;
                ns.setName(name);
                ns.setCode(code);
                ns.setCreditHours(hours);
                ns.setDepartment(dept);
                ns.setSection(section);

                boolean success;
                if (s == null) success = dao.insert(ns); 
                else success = dao.update(ns);

                if (success) {
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Database error: Could not save subject.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Credit Hours must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel layoutField(String label, JTextField f) {
        JPanel p = new JPanel(new BorderLayout(5, 5)); p.setOpaque(false);
        p.add(StyleConstants.createSecondaryLabel(label.toUpperCase()), BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        return p;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(StyleConstants.FONT_SMALL); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 8,15,8,15");
        return b;
    }
}
