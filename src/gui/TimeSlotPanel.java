package gui;

import database.TimeSlotDAO;
import models.TimeSlot;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TimeSlotPanel extends JPanel {
    private final TimeSlotDAO dao = new TimeSlotDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] TIMES = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};

    public TimeSlotPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(StyleConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Scheduling Grid");
        title.setFont(StyleConstants.FONT_TITLE);
        title.setForeground(StyleConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actionGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGrp.setOpaque(false);
        JButton addBtn = createActionButton("+ Define Slot", StyleConstants.SUCCESS);
        JButton delBtn = createActionButton("Delete", StyleConstants.DANGER);
        actionGrp.add(addBtn); actionGrp.add(delBtn);
        header.add(actionGrp, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Day of Week", "Start Time", "End Time"}, 0);
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
            if (JOptionPane.showConfirmDialog(this, "Remove this slot?") == 0) {
                dao.delete((int)tableModel.getValueAt(r, 0));
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (TimeSlot ts : dao.getAll()) 
            tableModel.addRow(new Object[]{ts.getId(), ts.getDay(), ts.getStartTime(), ts.getEndTime()});
    }

    private void showForm() {
        JPanel p = new JPanel(new GridLayout(3, 1, 15, 15));
        p.setBackground(StyleConstants.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> dayCB = new JComboBox<>(DAYS);
        JComboBox<String> startCB = new JComboBox<>(TIMES);
        JComboBox<String> endCB = new JComboBox<>(TIMES);
        dayCB.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        startCB.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        endCB.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        p.add(layoutCombo("Day", dayCB));
        p.add(layoutCombo("Start Time", startCB));
        p.add(layoutCombo("End Time", endCB));

        int res = JOptionPane.showConfirmDialog(this, p, "Define Time Slot", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (res == 0) {
            TimeSlot ts = new TimeSlot(0, (String)dayCB.getSelectedItem(), (String)startCB.getSelectedItem(), (String)endCB.getSelectedItem());
            dao.insert(ts);
            refresh();
        }
    }

    private JPanel layoutCombo(String label, JComboBox<String> cb) {
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
