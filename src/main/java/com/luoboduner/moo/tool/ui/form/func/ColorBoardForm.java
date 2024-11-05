package com.luoboduner.moo.tool.ui.form.func;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.luoboduner.moo.tool.App;
import com.luoboduner.moo.tool.ui.component.CustomizeIcon;
import com.luoboduner.moo.tool.ui.listener.func.ColorBoardListener;
import com.luoboduner.moo.tool.util.ColorUtil;
import com.luoboduner.moo.tool.util.UndoUtil;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * <pre>
 * ColorBoardForm
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">RememBerBer</a>
 * @since 2019/11/13.
 */
@Getter
public class ColorBoardForm {
    private JPanel colorBoardPanel;
    private JButton pickerButton;
    private JComboBox codeTypeComboBox;
    private JTextField colorCodeTextField;
    private JButton copyButton;
    private JPanel showColorPanel;
    private JButton favoriteButton;
    private JButton favoriteBookButton;
    private JPanel standardColorPanel;
    private JPanel themeColorPanel;
    private JComboBox themeComboBox;
    private JPanel themeColorMainPanel;
    private JPanel themeColorSubPanel;
    private JLabel aboutLabel;
    private JButton chooseColorButton;

    private static ColorBoardForm colorBoardForm;

    private static final Log logger = LogFactory.get();

    private static final Dimension DIMENSION_COLOR_BLOCK = new Dimension(50, 50);

    private static final Dimension DIMENSION_SUB_PANEL = new Dimension(50, 250);

    private ColorBoardForm() {
        UndoUtil.register(this);
    }

    public static void setColorCode(String code) {
        String codeType = (String) colorBoardForm.getCodeTypeComboBox().getSelectedItem();
        if (code.contains(",")) {
            code = ColorUtil.rgbStrToHex(code);
        }
        if ("HTML".equals(codeType)) {
            code = code.toUpperCase();
        } else if ("html".equals(codeType)) {
            code = code.toLowerCase();
        } else if ("RGB".equals(codeType)) {
            code = ColorUtil.toRgbStr(ColorUtil.fromHex(code));
        }
        colorBoardForm.getColorCodeTextField().setText(code);
    }

    public static ColorBoardForm getInstance() {
        if (colorBoardForm == null) {
            colorBoardForm = new ColorBoardForm();
        }
        return colorBoardForm;
    }

    public static void init() {
        colorBoardForm = getInstance();
        colorBoardForm.getColorCodeTextField().setText(App.config.getLastSelectedColor());
        colorBoardForm.getShowColorPanel().setBackground(ColorUtil.fromHex(App.config.getLastSelectedColor()));
        colorBoardForm.getCodeTypeComboBox().setSelectedItem(App.config.getColorCodeType());
        colorBoardForm.getThemeComboBox().setSelectedItem(App.config.getColorTheme());

        colorBoardForm.getFavoriteBookButton().setIcon(CustomizeIcon.favoriteBookBtnIcon);
        colorBoardForm.getFavoriteButton().setIcon(CustomizeIcon.favoriteBtnIcon);
        colorBoardForm.getPickerButton().setIcon(new FlatSVGIcon("icon/color_picker.svg"));
        colorBoardForm.getCopyButton().setIcon(new FlatSVGIcon("icon/copy.svg"));
        colorBoardForm.getChooseColorButton().setIcon(new FlatSVGIcon("icon/color.svg"));
        colorBoardForm.getAboutLabel().setIcon(new FlatSVGIcon("icon/help.svg"));

        fillColorBlocks();

        ColorBoardListener.addListeners();
    }

    public static void fillColorBlocks() {
        colorBoardForm.getStandardColorPanel().removeAll();
        for (String colorHex : ColorConsts.STANDARD) {
            JPanel panel = new JPanel();
            panel.setBackground(ColorUtil.fromHex(colorHex));
            panel.setSize(DIMENSION_COLOR_BLOCK);
            panel.setPreferredSize(DIMENSION_COLOR_BLOCK);
            addColorSelectionListener(panel);
            colorBoardForm.getStandardColorPanel().add(panel);
            colorBoardForm.getStandardColorPanel().updateUI();
        }

        String theme = (String) colorBoardForm.getThemeComboBox().getSelectedItem();

        String[] mainColors;
        String[][] subColors;
        if ("主题1".equals(theme)) {
            mainColors = ColorConsts.THEME_1_MAIN;
            subColors = ColorConsts.THEME_1_SUB;
        } else if ("主题2".equals(theme)) {
            mainColors = ColorConsts.THEME_2_MAIN;
            subColors = ColorConsts.THEME_2_SUB;
        } else if ("主题3".equals(theme)) {
            mainColors = ColorConsts.THEME_3_MAIN;
            subColors = ColorConsts.THEME_3_SUB;
        } else if ("主题4".equals(theme)) {
            mainColors = ColorConsts.THEME_4_MAIN;
            subColors = ColorConsts.THEME_4_SUB;
        } else if ("主题5".equals(theme)) {
            mainColors = ColorConsts.THEME_5_MAIN;
            subColors = ColorConsts.THEME_5_SUB;
        } else {
            mainColors = ColorConsts.THEME_DEFAULT_MAIN;
            subColors = ColorConsts.THEME_DEFAULT_SUB;
        }

        colorBoardForm.getThemeColorMainPanel().removeAll();
        for (String colorHex : mainColors) {
            JPanel panel = new JPanel();
            panel.setBackground(ColorUtil.fromHex(colorHex));
            panel.setSize(DIMENSION_COLOR_BLOCK);
            panel.setPreferredSize(DIMENSION_COLOR_BLOCK);
            addColorSelectionListener(panel);
            colorBoardForm.getThemeColorMainPanel().add(panel);
            colorBoardForm.getThemeColorMainPanel().updateUI();
        }

        colorBoardForm.getThemeColorSubPanel().removeAll();
        for (String[] colors : subColors) {
            JPanel subPanel = new JPanel();
            subPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            subPanel.setSize(DIMENSION_SUB_PANEL);
            subPanel.setPreferredSize(DIMENSION_SUB_PANEL);
            for (String colorHex : colors) {
                JPanel panel = new JPanel();
                panel.setBackground(ColorUtil.fromHex(colorHex));
                panel.setSize(DIMENSION_COLOR_BLOCK);
                panel.setPreferredSize(DIMENSION_COLOR_BLOCK);
                addColorSelectionListener(panel);
                subPanel.add(panel);
                colorBoardForm.getThemeColorSubPanel().updateUI();
            }
            colorBoardForm.getThemeColorSubPanel().add(subPanel);
            colorBoardForm.getThemeColorSubPanel().updateUI();
        }
    }

    private static void addColorSelectionListener(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel clickedPanel = (JPanel) e.getComponent();
                Color background = clickedPanel.getBackground();
                setSelectedColor(background);
                super.mouseClicked(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                JPanel enteredPanel = (JPanel) e.getComponent();
                enteredPanel.setBorder(BorderFactory.createEtchedBorder());
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JPanel enteredPanel = (JPanel) e.getComponent();
                enteredPanel.setBorder(BorderFactory.createEmptyBorder());
                super.mouseExited(e);
            }
        });
    }

    public static void setSelectedColor(Color color) {
        String hex = ColorUtil.toHex(color);
        colorBoardForm.getShowColorPanel().setBackground(color);
        colorBoardForm.getShowColorPanel().updateUI();
        setColorCode(hex);
        colorBoardForm.getColorCodeTextField().grabFocus();
        App.config.setLastSelectedColor(hex);
        App.config.save();
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
        colorBoardPanel = new JPanel();
        colorBoardPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        colorBoardPanel.setMinimumSize(new Dimension(400, 300));
        colorBoardPanel.setPreferredSize(new Dimension(400, 300));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(10, 10, 10, 10), -1, -1));
        colorBoardPanel.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        themeColorPanel = new JPanel();
        themeColorPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 5, 0), -1, -1));
        panel2.add(themeColorPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        themeColorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "主题颜色", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, themeColorPanel.getFont()), null));
        themeComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("默认");
        defaultComboBoxModel1.addElement("主题1");
        defaultComboBoxModel1.addElement("主题2");
        defaultComboBoxModel1.addElement("主题3");
        defaultComboBoxModel1.addElement("主题4");
        defaultComboBoxModel1.addElement("主题5");
        themeComboBox.setModel(defaultComboBoxModel1);
        themeColorPanel.add(themeComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        themeColorMainPanel = new JPanel();
        themeColorMainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        themeColorPanel.add(themeColorMainPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        themeColorSubPanel = new JPanel();
        themeColorSubPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        themeColorPanel.add(themeColorSubPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        standardColorPanel = new JPanel();
        standardColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel2.add(standardColorPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        standardColorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "标准颜色", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, standardColorPanel.getFont()), null));
        showColorPanel = new JPanel();
        showColorPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 10, 0, 0), -1, -1));
        panel1.add(showColorPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        showColorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 9, new Insets(10, 10, 10, 10), -1, -1));
        colorBoardPanel.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pickerButton = new JButton();
        pickerButton.setIcon(new ImageIcon(getClass().getResource("/icon/color_picker.png")));
        pickerButton.setText("取色器");
        panel3.add(pickerButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        codeTypeComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("HTML");
        defaultComboBoxModel2.addElement("html");
        defaultComboBoxModel2.addElement("RGB");
        codeTypeComboBox.setModel(defaultComboBoxModel2);
        panel3.add(codeTypeComboBox, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        colorCodeTextField = new JTextField();
        panel3.add(colorCodeTextField, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        copyButton = new JButton();
        copyButton.setIcon(new ImageIcon(getClass().getResource("/icon/copy.png")));
        copyButton.setText("复制");
        panel3.add(copyButton, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        favoriteButton = new JButton();
        favoriteButton.setIcon(new ImageIcon(getClass().getResource("/icon/star-empty.png")));
        favoriteButton.setText("收藏");
        panel3.add(favoriteButton, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        favoriteBookButton = new JButton();
        favoriteBookButton.setIcon(new ImageIcon(getClass().getResource("/icon/favorite.png")));
        favoriteBookButton.setText("收藏夹");
        panel3.add(favoriteBookButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aboutLabel = new JLabel();
        aboutLabel.setIcon(new ImageIcon(getClass().getResource("/icon/helpButton.png")));
        aboutLabel.setText("");
        panel3.add(aboutLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chooseColorButton = new JButton();
        chooseColorButton.setText("自由选色");
        panel3.add(chooseColorButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        colorBoardPanel.add(separator1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
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
        return colorBoardPanel;
    }

}
