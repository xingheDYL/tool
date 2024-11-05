package com.luoboduner.moo.tool.ui.dialog;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.luoboduner.moo.tool.App;
import com.luoboduner.moo.tool.service.HttpMsgSender;
import com.luoboduner.moo.tool.ui.component.TopMenuBar;
import com.luoboduner.moo.tool.ui.form.MainWindow;
import com.luoboduner.moo.tool.ui.form.func.*;
import com.luoboduner.moo.tool.ui.frame.MainFrame;
import com.luoboduner.moo.tool.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Locale;

public class SettingDialog extends JDialog {

    private static final Log logger = LogFactory.get();

    private JPanel contentPane;
    private JScrollPane settingScrollPane;
    private JCheckBox autoCheckUpdateCheckBox;
    private JComboBox comboBox1;
    private JComboBox menuBarPositionComboBox;
    private JTextField dbFilePathTextField;
    private JButton dbFilePathExploreButton;
    private JButton dbFilePathSaveButton;
    private JButton httpSaveButton;
    private JCheckBox httpUseProxyCheckBox;
    private JPanel httpProxyPanel;
    private JTextField httpProxyHostTextField;
    private JTextField httpProxyPortTextField;
    private JTextField httpProxyUserTextField;
    private JTextField httpProxyPasswordTextField;
    private JComboBox sqlDialectComboBox;
    private JComboBox fontFamilyComboBox;
    private JPanel accentColorPanel;
    private JComboBox funcTabPositionComboBox;
    private JCheckBox tabCompactCheckBox;
    private JCheckBox tabSeparatorCheckBox;
    private JCheckBox tabHideTitleCheckBox;
    private JCheckBox tabCardCheckBox;
    private JToolBar toolBar;
    public static String[] accentColorKeys = {
            "Moo.accent.default",
            "Moo.accent.blue",
            "Moo.accent.purple",
            "Moo.accent.red", "Moo.accent.mooRed",
            "Moo.accent.orange",
            "Moo.accent.yellow", "Moo.accent.mooYellow", "Moo.accent.mooYellow2",
            "Moo.accent.mooGreen", "Moo.accent.green", "Moo.accent.weGreen"
    };
    private static String[] accentColorNames = {
            "Default", "Blue", "Purple", "Red", "Orange", "Yellow", "Green", "MooYellow"
    };
    private final JToggleButton[] accentColorButtons = new JToggleButton[accentColorKeys.length];

    public SettingDialog() {

        super(App.mainFrame, "设置");
        ComponentUtil.setPreferSizeAndLocateToCenter(this, 0.5, 0.68);
        setContentPane(contentPane);
        setModal(true);

        if (SystemUtil.isMacOs() && SystemInfo.isMacFullWindowContentSupported) {
            this.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            this.getRootPane().putClientProperty("apple.awt.fullscreenable", true);
            this.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
            GridLayoutManager gridLayoutManager = (GridLayoutManager) contentPane.getLayout();
            gridLayoutManager.setMargin(new Insets(28, 0, 0, 0));
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onOK(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 设置滚动条速度
        ScrollUtil.smoothPane(settingScrollPane);

        dbFilePathSaveButton.setIcon(new FlatSVGIcon("icon/save.svg"));
        httpSaveButton.setIcon(new FlatSVGIcon("icon/save.svg"));

        initAccentColors();

        // 常规
        autoCheckUpdateCheckBox.setSelected(App.config.isAutoCheckUpdate());

        // HTTP请求
        httpUseProxyCheckBox.setSelected(App.config.isHttpUseProxy());
        httpProxyHostTextField.setText(App.config.getHttpProxyHost());
        httpProxyPortTextField.setText(App.config.getHttpProxyPort());
        httpProxyUserTextField.setText(App.config.getHttpProxyUserName());
        httpProxyPasswordTextField.setText(App.config.getHttpProxyPassword());

        toggleHttpProxyPanel();

        // 使用习惯
        menuBarPositionComboBox.setSelectedItem(App.config.getMenuBarPosition());
        funcTabPositionComboBox.setSelectedItem(App.config.getFuncTabPosition());

        // 功能Tab样式
        tabCompactCheckBox.setSelected(App.config.isTabCompact());
        tabSeparatorCheckBox.setSelected(App.config.isTabSeparator());
        tabHideTitleCheckBox.setSelected(App.config.isTabHideTitle());
        tabCardCheckBox.setSelected(App.config.isTabCard());

        // sql dialect
        sqlDialectComboBox.setSelectedItem(App.config.getSqlDialect());

        // 字体
        initFontFamilyMenu();
        fontFamilyComboBox.setSelectedItem(App.config.getFont());

        // 高级
        dbFilePathTextField.setText(App.config.getDbFilePath());

        contentPane.updateUI();

        // 设置-常规-启动时自动检查更新
        autoCheckUpdateCheckBox.addActionListener(e -> {
            App.config.setAutoCheckUpdate(autoCheckUpdateCheckBox.isSelected());
            App.config.save();
        });

        httpSaveButton.addActionListener(e -> {
            try {
                App.config.setHttpUseProxy(httpUseProxyCheckBox.isSelected());
                App.config.setHttpProxyHost(httpProxyHostTextField.getText());
                App.config.setHttpProxyPort(httpProxyPortTextField.getText());
                App.config.setHttpProxyUserName(httpProxyUserTextField.getText());
                App.config.setHttpProxyPassword(httpProxyPasswordTextField.getText());
                App.config.save();

                HttpMsgSender.proxy = null;
                AlertUtil.buttonInfo(httpSaveButton, "保存", "保存成功", 2000);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(contentPane, "保存失败！\n\n" + e1.getMessage(), "失败",
                        JOptionPane.ERROR_MESSAGE);
                logger.error(e1);
            }
        });

        httpUseProxyCheckBox.addChangeListener(e -> toggleHttpProxyPanel());

        // 使用习惯-菜单栏位置
        menuBarPositionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                App.config.setMenuBarPosition(e.getItem().toString());
                App.config.save();
                QuickNoteForm.init();
                JsonBeautyForm.init();
                HostForm.init();
                HttpRequestForm.init();
                QrCodeForm.init();
                CryptoForm.init();
                CryptoForm1.init();
            }
        });

        // 使用习惯-功能面板位置
        funcTabPositionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                App.config.setFuncTabPosition(e.getItem().toString());
                App.config.save();
                MainWindow.getInstance().initTabPlacement();
            }
        });

        // 功能Tab样式
        tabCompactCheckBox.addItemListener(e -> {
            App.config.setTabCompact(e.getStateChange() == ItemEvent.SELECTED);
            App.config.save();
            MainWindow.getInstance().initTabPlacement();
        });
        tabSeparatorCheckBox.addItemListener(e -> {
            App.config.setTabSeparator(e.getStateChange() == ItemEvent.SELECTED);
            App.config.save();
            MainWindow.getInstance().initTabPlacement();
        });
        tabHideTitleCheckBox.addItemListener(e -> {
            App.config.setTabHideTitle(e.getStateChange() == ItemEvent.SELECTED);
            App.config.save();
            MainWindow.getInstance().initTabPlacement();
        });
        tabCardCheckBox.addItemListener(e -> {
            App.config.setTabCard(e.getStateChange() == ItemEvent.SELECTED);
            App.config.save();
            MainWindow.getInstance().initTabPlacement();
        });

        sqlDialectComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                App.config.setSqlDialect(e.getItem().toString());
                App.config.save();
            }
        });

        fontFamilyComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                App.config.setFont(e.getItem().toString());
                App.config.save();
                MainFrame.topMenuBar.fontFamilyChanged(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, e.getItem().toString()));
            }
        });

        // 高级 数据文件路径设置
        dbFilePathSaveButton.addActionListener(e -> {
            try {
                String dbFilePath = dbFilePathTextField.getText();

                // 复制之前的数据文件到新位置
                String dbFilePathBefore = App.config.getDbFilePathBefore();
                if (dbFilePathBefore.equals(dbFilePath)) {
                    JOptionPane.showMessageDialog(contentPane, "保存成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if (StringUtils.isBlank(dbFilePathBefore)) {
                    dbFilePathBefore = SystemUtil.CONFIG_HOME;
                }
                if (StringUtils.isNotBlank(dbFilePath)) {
                    FileUtil.copy(dbFilePathBefore + File.separator + "MooTool.db", dbFilePath, false);
                }

                MybatisUtil.setSqlSession(null);

                App.config.setDbFilePath(dbFilePath);
                App.config.setDbFilePathBefore(dbFilePath);
                App.config.save();
                JOptionPane.showMessageDialog(contentPane, "保存成功！\n\n需要重启MooTool生效", "成功", JOptionPane.INFORMATION_MESSAGE);
                JOptionPane.showMessageDialog(contentPane, "MooTool即将关闭！\n\n关闭后需要手动再次打开", "MooTool即将关闭", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(contentPane, "保存失败！\n\n" + e1.getMessage(), "失败", JOptionPane.ERROR_MESSAGE);
                logger.error(ExceptionUtils.getStackTrace(e1));
            }
        });

        dbFilePathExploreButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(dbFilePathTextField.getText());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int approve = fileChooser.showOpenDialog(contentPane);
            String dbFilePath;
            if (approve == JFileChooser.APPROVE_OPTION) {
                dbFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                dbFilePathTextField.setText(dbFilePath);
            }
        });

    }

    private void onOK() {
        // add your code here
        dispose();
    }

    /**
     * 切换HTTP代理设置面板显示/隐藏
     */
    private void toggleHttpProxyPanel() {

        boolean httpUseProxy = httpUseProxyCheckBox.isSelected();
        if (httpUseProxy) {
            httpProxyPanel.setVisible(true);
        } else {
            httpProxyPanel.setVisible(false);
        }
    }

    private void initFontFamilyMenu() {

        fontFamilyComboBox.removeAllItems();
        for (String font : TopMenuBar.fontNames) {
            fontFamilyComboBox.addItem(font);
        }
    }

    private void initAccentColors() {
        toolBar = new JToolBar();

        toolBar.add(Box.createHorizontalGlue());

        ButtonGroup group = new ButtonGroup();
        int selectedIndex = 0;
        for (int i = 0; i < accentColorButtons.length; i++) {
            String accentColorKey = accentColorKeys[i];
            accentColorButtons[i] = new JToggleButton(new AccentColorIcon(accentColorKey));
            accentColorButtons[i].setToolTipText("仅FlatLight、FlatDark、FlatIntelliJ、FlatDarcula等主题支持设置强调色");
            accentColorButtons[i].addActionListener(this::accentColorChanged);
            if (accentColorKey.equals(App.config.getAccentColor())) {
                selectedIndex = i;
            }
            toolBar.add(accentColorButtons[i]);
            group.add(accentColorButtons[i]);
        }

        accentColorButtons[selectedIndex].setSelected(true);

        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName()))
                updateAccentColorButtons();
        });
        updateAccentColorButtons();

        accentColorPanel.add(toolBar);
    }

    /**
     * codes are copied from FlatLaf/flatlaf-demo/ (https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-demo)
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     * <p>
     * https://www.apache.org/licenses/LICENSE-2.0
     * <p>
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private void accentColorChanged(ActionEvent e) {
        String accentColor = accentColorKeys[0];

        for (int i = 0; i < accentColorButtons.length; i++) {
            if (accentColorButtons[i].isSelected()) {
                accentColor = accentColorKeys[i];
                App.config.setAccentColor(accentColor);
                App.config.save();
                break;
            }
        }

        FlatLaf.setGlobalExtraDefaults((!accentColor.equals(accentColorKeys[0]))
                ? Collections.singletonMap("@accentColor", "$" + accentColor)
                : null);

        Class<? extends LookAndFeel> lafClass = UIManager.getLookAndFeel().getClass();
        try {
            FlatLaf.setup(lafClass.getDeclaredConstructor().newInstance());
            FlatLaf.updateUI();
        } catch (InstantiationException | IllegalAccessException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        } catch (InvocationTargetException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * codes are copied from FlatLaf/flatlaf-demo/ (https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-demo)
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     * <p>
     * https://www.apache.org/licenses/LICENSE-2.0
     * <p>
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private void updateAccentColorButtons() {
        Class<? extends LookAndFeel> lafClass = UIManager.getLookAndFeel().getClass();
        boolean isAccentColorSupported =
                lafClass == FlatLightLaf.class ||
                        lafClass == FlatDarkLaf.class ||
                        lafClass == FlatIntelliJLaf.class ||
                        lafClass == FlatDarculaLaf.class ||
                        lafClass == FlatMacLightLaf.class ||
                        lafClass == FlatMacDarkLaf.class;

        for (int i = 0; i < accentColorButtons.length; i++)
            accentColorButtons[i].setEnabled(isAccentColorSupported);
    }

    /**
     * codes are copied from FlatLaf/flatlaf-demo/ (https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-demo)
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     * <p>
     * https://www.apache.org/licenses/LICENSE-2.0
     * <p>
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private static class AccentColorIcon
            extends FlatAbstractIcon {
        private final String colorKey;

        AccentColorIcon(String colorKey) {
            super(16, 16, null);
            this.colorKey = colorKey;
        }

        @Override
        protected void paintIcon(Component c, Graphics2D g) {
            Color color = UIManager.getColor(colorKey);
            if (color == null)
                color = Color.lightGray;
            else if (!c.isEnabled()) {
                color = FlatLaf.isLafDark()
                        ? ColorFunctions.shade(color, 0.5f)
                        : ColorFunctions.tint(color, 0.6f);
            }

            g.setColor(color);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        settingScrollPane = new JScrollPane();
        contentPane.add(settingScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        settingScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(20, 20, 10, 10), -1, -1));
        settingScrollPane.setViewportView(panel1);
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(600, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(15, 15, 25, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "常规", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, panel3.getFont()), null));
        autoCheckUpdateCheckBox = new JCheckBox();
        autoCheckUpdateCheckBox.setText("启动时自动检查更新");
        panel3.add(autoCheckUpdateCheckBox, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("语言");
        panel3.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("字体");
        panel3.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBox1 = new JComboBox();
        comboBox1.setEnabled(false);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("简体中文");
        defaultComboBoxModel1.addElement("English");
        comboBox1.setModel(defaultComboBoxModel1);
        panel3.add(comboBox1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fontFamilyComboBox = new JComboBox();
        fontFamilyComboBox.setEnabled(true);
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        fontFamilyComboBox.setModel(defaultComboBoxModel2);
        panel3.add(fontFamilyComboBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        accentColorPanel = new JPanel();
        accentColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel3.add(accentColorPanel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("强调色");
        panel3.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(15, 15, 25, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), " 随手记", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, panel4.getFont()), null));
        final JLabel label4 = new JLabel();
        label4.setText("SQL\"方言\"(dialect)");
        panel4.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sqlDialectComboBox = new JComboBox();
        sqlDialectComboBox.setEnabled(true);
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Standard SQL");
        defaultComboBoxModel3.addElement("MariaDB");
        defaultComboBoxModel3.addElement("MySQL");
        defaultComboBoxModel3.addElement("PostgreSQL");
        defaultComboBoxModel3.addElement("IBM DB2");
        defaultComboBoxModel3.addElement("Oracle PL/SQL");
        defaultComboBoxModel3.addElement("Couchbase N1QL");
        defaultComboBoxModel3.addElement("Amazon Redshift");
        defaultComboBoxModel3.addElement("Spark");
        defaultComboBoxModel3.addElement("SQL Server Transact-SQL");
        sqlDialectComboBox.setModel(defaultComboBoxModel3);
        panel4.add(sqlDialectComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(15, 15, 25, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "使用习惯", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, panel5.getFont()), null));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("菜单栏(按钮操作区)位置");
        panel6.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel6.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        menuBarPositionComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("上方");
        defaultComboBoxModel4.addElement("下方");
        menuBarPositionComboBox.setModel(defaultComboBoxModel4);
        panel6.add(menuBarPositionComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("功能Tab位置");
        panel7.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel7.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        funcTabPositionComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel5 = new DefaultComboBoxModel();
        defaultComboBoxModel5.addElement("上方");
        defaultComboBoxModel5.addElement("左侧");
        funcTabPositionComboBox.setModel(defaultComboBoxModel5);
        panel7.add(funcTabPositionComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(15, 15, 25, 0), -1, -1));
        panel2.add(panel8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "功能Tab样式", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, panel8.getFont()), null));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel9.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        tabCompactCheckBox = new JCheckBox();
        tabCompactCheckBox.setText("紧凑");
        panel9.add(tabCompactCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tabHideTitleCheckBox = new JCheckBox();
        tabHideTitleCheckBox.setText("隐藏标题");
        panel9.add(tabHideTitleCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tabSeparatorCheckBox = new JCheckBox();
        tabSeparatorCheckBox.setText("显示分割线");
        panel9.add(tabSeparatorCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tabCardCheckBox = new JCheckBox();
        tabCardCheckBox.setText("卡片页签");
        panel9.add(tabCardCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(2, 3, new Insets(15, 15, 25, 0), -1, -1));
        panel2.add(panel10, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel10.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "高级", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, panel10.getFont()), null));
        final JLabel label7 = new JLabel();
        label7.setText("数据存储位置");
        panel10.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dbFilePathTextField = new JTextField();
        panel10.add(dbFilePathTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        dbFilePathExploreButton = new JButton();
        dbFilePathExploreButton.setText("…");
        panel10.add(dbFilePathExploreButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        dbFilePathSaveButton = new JButton();
        dbFilePathSaveButton.setIcon(new ImageIcon(getClass().getResource("/icon/menu-saveall_dark.png")));
        dbFilePathSaveButton.setText("保存");
        panel11.add(dbFilePathSaveButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel11.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(3, 1, new Insets(15, 15, 25, 0), -1, -1));
        Font panel12Font = this.$$$getFont$$$("Microsoft YaHei UI", -1, -1, panel12.getFont());
        if (panel12Font != null) panel12.setFont(panel12Font);
        panel2.add(panel12, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel12.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "HTTP请求", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, panel12.getFont()), null));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel12.add(panel13, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        httpSaveButton = new JButton();
        httpSaveButton.setIcon(new ImageIcon(getClass().getResource("/icon/menu-saveall_dark.png")));
        httpSaveButton.setText("保存");
        panel13.add(httpSaveButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel13.add(spacer8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        httpUseProxyCheckBox = new JCheckBox();
        httpUseProxyCheckBox.setText("使用HTTP代理");
        panel12.add(httpUseProxyCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        httpProxyPanel = new JPanel();
        httpProxyPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 26, 0, 0), -1, -1));
        panel12.add(httpProxyPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Host");
        httpProxyPanel.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        httpProxyHostTextField = new JTextField();
        httpProxyPanel.add(httpProxyHostTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("端口");
        httpProxyPanel.add(label9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("用户名");
        httpProxyPanel.add(label10, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("密码");
        httpProxyPanel.add(label11, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        httpProxyPortTextField = new JTextField();
        httpProxyPanel.add(httpProxyPortTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        httpProxyUserTextField = new JTextField();
        httpProxyPanel.add(httpProxyUserTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        httpProxyPasswordTextField = new JTextField();
        httpProxyPanel.add(httpProxyPasswordTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel1.add(spacer9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
