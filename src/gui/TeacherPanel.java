package gui;

import database.TeacherDAO;
import models.Teacher;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class TeacherPanel extends JPanel {

    private final TeacherDAO        dao = new TeacherDAO();
    private final DefaultTableModel tableModel;
    private final JTable            table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField        searchField;
    private final JLabel            countLbl = new JLabel();

    public TeacherPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(StyleConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // ── Header: title + action buttons ───────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel title = new JLabel("Teacher Directory");
        title.setFont(StyleConstants.FONT_TITLE);
        title.setForeground(StyleConstants.TEXT_PRIMARY);

        JLabel subtitle = StyleConstants.createSecondaryLabel("Manage faculty records and availability");
        subtitle.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        titleGroup.add(title);
        titleGroup.add(subtitle);
        header.add(titleGroup, BorderLayout.WEST);

        JPanel actionGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGrp.setOpaque(false);

        JButton addBtn  = actionBtn("+ Add Teacher", StyleConstants.SUCCESS);
        JButton editBtn = actionBtn("Edit",          StyleConstants.ACCENT);
        JButton delBtn  = actionBtn("Delete",        StyleConstants.DANGER);

        actionGrp.add(addBtn);
        actionGrp.add(editBtn);
        actionGrp.add(delBtn);
        header.add(actionGrp, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Search row ────────────────────────────────────────────────────
        searchField = new JTextField();
        StyleConstants.applyFieldStyle(searchField, "🔍  Search by name or specialization…");

        countLbl.setFont(StyleConstants.FONT_SMALL);
        countLbl.setForeground(StyleConstants.TEXT_MUTED);

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        searchRow.add(searchField, BorderLayout.WEST);
        searchRow.add(countLbl,   BorderLayout.EAST);
        searchField.setPreferredSize(new Dimension(340, 36));

        // ── Table ─────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Full Name", "Specialization", "Availability"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setRowHeight(42);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(StyleConstants.BORDER);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(StyleConstants.BG_CARD);
        table.getTableHeader().setForeground(StyleConstants.TEXT_SECONDARY);
        table.getTableHeader().setFont(StyleConstants.FONT_LABEL);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setSelectionBackground(new Color(
            StyleConstants.ACCENT.getRed(), StyleConstants.ACCENT.getGreen(),
            StyleConstants.ACCENT.getBlue(), 55));
        table.setSelectionForeground(StyleConstants.TEXT_PRIMARY);
        table.putClientProperty(FlatClientProperties.STYLE,
            "arc: 12; border: 1,1,1,1," + StyleConstants.toHex(StyleConstants.BORDER));

        // Hide the raw ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new javax.swing.border.LineBorder(StyleConstants.BORDER, 1, true));
        sp.getViewport().setBackground(StyleConstants.BG_CARD);

        // Live search filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(searchRow, BorderLayout.NORTH);
        center.add(sp,        BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ── Button actions ────────────────────────────────────────────────
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { hint(); return; }
            int mr = table.convertRowIndexToModel(row);
            showForm(new Teacher(
                (int)    tableModel.getValueAt(mr, 0),
                (String) tableModel.getValueAt(mr, 1),
                (String) tableModel.getValueAt(mr, 2),
                (String) tableModel.getValueAt(mr, 3)));
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { hint(); return; }
            int mr = table.convertRowIndexToModel(row);
            if (JOptionPane.showConfirmDialog(this,
                "Remove this teacher record permanently?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == 0) {
                dao.delete((int) tableModel.getValueAt(mr, 0));
                refresh();
            }
        });

        refresh();
    }

    // ── Data helpers ──────────────────────────────────────────────────────

    private void refresh() {
        tableModel.setRowCount(0);
        for (Teacher t : dao.getAll())
            tableModel.addRow(new Object[]{
                t.getId(), t.getName(), t.getSpecialization(), t.getAvailability()});
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1, 2));
        countLbl.setText(table.getRowCount() + " record(s)");
    }

    // ── Form dialog ───────────────────────────────────────────────────────

    private static final String[] DAYS_OF_WEEK =
        {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private void showForm(Teacher t) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(StyleConstants.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTextField nameF = new JTextField(t != null ? t.getName()           : "");
        JTextField specF = new JTextField(t != null ? t.getSpecialization() : "");

        StyleConstants.applyFieldStyle(nameF, "Full Name");
        StyleConstants.applyFieldStyle(specF, "e.g. Computer Science");

        Dimension fieldSize = new Dimension(380, 38);
        nameF.setPreferredSize(fieldSize);
        specF.setPreferredSize(fieldSize);

        // ── Availability day-checkboxes ──────────────────────────────────
        String existingAvail = (t != null && t.getAvailability() != null) ? t.getAvailability() : "";
        JCheckBox[] dayBoxes = new JCheckBox[DAYS_OF_WEEK.length];
        JPanel daysPanel = new JPanel(new java.awt.GridLayout(2, 3, 8, 6));
        daysPanel.setOpaque(false);
        daysPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        for (int i = 0; i < DAYS_OF_WEEK.length; i++) {
            dayBoxes[i] = new JCheckBox(DAYS_OF_WEEK[i]);
            dayBoxes[i].setFont(StyleConstants.FONT_BODY);
            dayBoxes[i].setForeground(StyleConstants.TEXT_PRIMARY);
            dayBoxes[i].setOpaque(false);
            // Pre-check days that appear in the existing availability string
            String lower = existingAvail.toLowerCase();
            String dayLower = DAYS_OF_WEEK[i].toLowerCase();
            dayBoxes[i].setSelected(lower.contains(dayLower)
                || lower.equalsIgnoreCase("all")
                || lower.equalsIgnoreCase("all days")
                || lower.equalsIgnoreCase("available"));
            daysPanel.add(dayBoxes[i]);
        }

        // "Select All" / "Clear All" quick links
        JPanel quickLinks = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        quickLinks.setOpaque(false);
        JButton selAll   = linkBtn("Select All");
        JButton clearAll = linkBtn("Clear All");
        selAll.addActionListener(e -> { for (JCheckBox cb : dayBoxes) cb.setSelected(true); });
        clearAll.addActionListener(e -> { for (JCheckBox cb : dayBoxes) cb.setSelected(false); });
        quickLinks.add(selAll);
        JLabel sep = new JLabel("  |  ");
        sep.setForeground(StyleConstants.TEXT_MUTED);
        sep.setFont(StyleConstants.FONT_SMALL);
        quickLinks.add(sep);
        quickLinks.add(clearAll);

        JPanel availBlock = new JPanel();
        availBlock.setLayout(new BoxLayout(availBlock, BoxLayout.Y_AXIS));
        availBlock.setOpaque(false);
        JLabel availLbl = new JLabel("AVAILABLE DAYS");
        availLbl.setFont(StyleConstants.FONT_LABEL);
        availLbl.setForeground(StyleConstants.TEXT_SECONDARY);
        availBlock.add(availLbl);
        availBlock.add(Box.createVerticalStrut(6));
        availBlock.add(daysPanel);
        availBlock.add(Box.createVerticalStrut(4));
        availBlock.add(quickLinks);

        p.add(fieldBlock("Full Name",      nameF));
        p.add(Box.createVerticalStrut(14));
        p.add(fieldBlock("Specialization", specF));
        p.add(Box.createVerticalStrut(18));
        p.add(availBlock);

        int res = JOptionPane.showConfirmDialog(this, p,
            t == null ? "Register New Teacher" : "Update Teacher",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            // Build availability string from checked boxes
            java.util.StringJoiner sj = new java.util.StringJoiner(", ");
            for (int i = 0; i < DAYS_OF_WEEK.length; i++)
                if (dayBoxes[i].isSelected()) sj.add(DAYS_OF_WEEK[i]);
            String availability = sj.toString().isEmpty() ? "None" : sj.toString();

            Teacher rec = (t == null) ? new Teacher() : t;
            rec.setName(nameF.getText().trim());
            rec.setSpecialization(specF.getText().trim());
            rec.setAvailability(availability);
            if (t == null) dao.insert(rec); else dao.update(rec);
            refresh();
        }
    }

    private JButton linkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(StyleConstants.FONT_SMALL);
        b.setForeground(StyleConstants.ACCENT);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    // ── Component factories ───────────────────────────────────────────────

    private JPanel fieldBlock(String label, JTextField field) {
        JPanel block = new JPanel(new BorderLayout(0, 5));
        block.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(StyleConstants.FONT_LABEL);
        lbl.setForeground(StyleConstants.TEXT_SECONDARY);
        block.add(lbl,   BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Inter", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 8,16,8,16");
        return b;
    }

    private void hint() {
        JOptionPane.showMessageDialog(this,
            "Please select a row first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }
}
