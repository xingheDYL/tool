package com.luoboduner.moo.tool.ui.frame;

import com.luoboduner.moo.tool.ui.UiConsts;
import com.luoboduner.moo.tool.ui.form.func.ColorPickerForm;
import com.luoboduner.moo.tool.ui.listener.ScreenMouseListener;

import javax.swing.*;
import java.awt.*;

/**
 * <pre>
 * ColorPickerFrame
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">RememBerBer</a>
 * @since 2019/11/19.
 */
public class ColorPickerFrame extends JFrame {

    private static ColorPickerFrame colorPickerFrame;

    private ColorPickerFrame() {
    }

    public static ColorPickerFrame getInstance() {
        if (colorPickerFrame == null) {
            colorPickerFrame = new ColorPickerFrame();
            colorPickerFrame.init();
        }
        return colorPickerFrame;
    }

    public static void showPicker() {
        Robot robot = ScreenMouseListener.getRobot();
        if (robot == null) {
            return;
        }
        ScreenFrame.getInstance().setVisible(true);
        ScreenFrame.getInstance().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        ColorPickerFrame.getInstance().setVisible(true);
    }

    public static void exit() {
        getInstance().setVisible(false);
        colorPickerFrame = null;
    }

    private void init() {
        setUndecorated(true);
        setAutoRequestFocus(false);
        setAlwaysOnTop(true);

        setName(UiConsts.APP_NAME);
        setTitle(UiConsts.APP_NAME + "-ColorPicker");
        FrameUtil.setFrameIcon(this);

        setContentPane(ColorPickerForm.getInstance().getColorPickerPanel());
        pack();
        setVisible(true);

        setLocation(10, 10);
    }

}
