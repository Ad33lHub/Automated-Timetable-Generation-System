package gui;

import database.ClassroomDAO;
import models.Classroom;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClassroomPanel extends JPanel {
    private final ClassroomDAO dao = new ClassroomDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private static final String[] TYPES = {"Lecture Hall", "Laboratory", "Seminar Room"};

    public ClassroomPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(StyleConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Facility Inventory");
        title.setFont(StyleConstants.FONT_TITLE);
        title.setForeground(StyleConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actionGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGrp.setOpaque(false);
        JButton addBtn = createActionButton("+ Add Room", StyleConstants.SUCCESS);
        JButton editBtn = createActionButton("Edit", StyleConstants.ACCENT);
        JButton delBtn = createActionButton("Delete", StyleConstants.DANGER);
        actionGrp.add(addBtn); actionGrp.add(editBtn); actionGrp.add(delBtn);
        header.add(actionGrp, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Room #", "Capacity", "Type"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().setReorderingAllowed(false);
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
            showForm(new Classroom((int)tableModel.getValueAt(r, 0), (String)tableModel.getValueAt(r, 1), 
                (int)tableModel.getValueAt(r, 2), (String)tableModel.getValueAt(r, 3)));
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) return;
            if (JOptionPane.showConfirmDialog(this, "Remove this room?") == 0) {
                dao.delete((int)tableModel.getValueAt(r, 0));
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (Classroom c : dao.getAll()) 
            tableModel.addRow(new Object[]{c.getId(), c.getRoomNumber(), c.getCapacity(), c.getType()});
    }

    private void showForm(Classroom c) {
        JPanel p = new JPanel(new GridLayout(3, 1, 15, 15));
        p.setBackground(StyleConstants.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField roomF = new JTextField();
        JTextField capF = new JTextField();
        JComboBox<String> typeCB = new JComboBox<>(TYPES);
        StyleConstants.applyFieldStyle(roomF, "Room Number");
        StyleConstants.applyFieldStyle(capF, "Capacity");
        typeCB.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        if (c != null) {
            roomF.setText(c.getRoomNumber());
            capF.setText(String.valueOf(c.getCapacity()));
            typeCB.setSelectedItem(c.getType());
        }

        p.add(layoutField("Room Number", roomF));
        p.add(layoutField("Capacity", capF));
        JPanel typeP = new JPanel(new BorderLayout(5, 5)); typeP.setOpaque(false);
        typeP.add(StyleConstants.createSecondaryLabel("ROOM TYPE"), BorderLayout.NORTH);
        typeP.add(typeCB, BorderLayout.CENTER);
        p.add(typeP);

        int res = JOptionPane.showConfirmDialog(this, p, c == null ? "Register Room" : "Update Room", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (res == 0) {
            Classroom nc = c == null ? new Classroom() : c;
            nc.setRoomNumber(roomF.getText());
            nc.setCapacity(Integer.parseInt(capF.getText()));
            nc.setType((String)typeCB.getSelectedItem());
            if (c == null) dao.insert(nc); else dao.update(nc);
            refresh();
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
