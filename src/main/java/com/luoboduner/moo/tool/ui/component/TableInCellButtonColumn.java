package com.luoboduner.moo.tool.ui.component;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.luoboduner.moo.tool.App;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <pre>
 * 自定义单元格按钮渲染器
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">Zhou Bo</a>
 * @since 2019/3/26.
 */
public class TableInCellButtonColumn extends AbstractCellEditor implements
        TableCellRenderer, TableCellEditor, ActionListener {
    private JTable table;
    private JButton renderButton;
    private JButton editButton;

    public TableInCellButtonColumn(JTable table, int column) {
        super();
        this.table = table;
        renderButton = new JButton();
        editButton = new JButton();
        editButton.setFocusPainted(false);
        editButton.addActionListener(this);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (hasFocus) {
            renderButton.setForeground(table.getForeground());
            renderButton.setBackground(UIManager.getColor("Button.background"));
        } else if (isSelected) {
            renderButton.setForeground(table.getSelectionForeground());
            renderButton.setBackground(table.getSelectionBackground());
        } else {
            renderButton.setForeground(table.getForeground());
            renderButton.setBackground(UIManager.getColor("Button.background"));
        }

        renderButton.setText("");
        renderButton.setIcon(new FlatSVGIcon("icon/remove.svg"));
        return renderButton;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        editButton.setText("");
        editButton.setIcon(new FlatSVGIcon("icon/remove.svg"));
        return editButton;
    }

    @Override
    public Object getCellEditorValue() {
        return "remove";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int isDelete = JOptionPane.showConfirmDialog(App.mainFrame, "确定移除？", "请确认",
                JOptionPane.YES_NO_OPTION);
        if (isDelete == JOptionPane.YES_OPTION) {
            fireEditingStopped();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.removeRow(table.getSelectedRow());
        }
    }
}