package com.luoboduner.moo.tool.ui.listener.func;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.luoboduner.moo.tool.App;
import com.luoboduner.moo.tool.ui.form.func.QrCodeForm;
import com.luoboduner.moo.tool.util.ConsoleUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * <pre>
 * QrCodeListener
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">RememBerBer</a>
 * @since 2019/10/15.
 */
public class QrCodeListener {
    private static final Log logger = LogFactory.get();

    public static void addListeners() {
        QrCodeForm qrCodeForm = QrCodeForm.getInstance();
        qrCodeForm.getGenerateButton().addActionListener(e -> ThreadUtil.execute(() -> {
            try {
                QrCodeForm.generate(true);
            } catch (Exception e1) {
                logger.error(e1);
            }
        }));
        qrCodeForm.getExploreButton().addActionListener(e -> {
            File beforeFile = new File(qrCodeForm.getLogoPathTextField().getText());
            JFileChooser fileChooser;

            if (beforeFile.exists()) {
                fileChooser = new JFileChooser(beforeFile);
            } else {
                fileChooser = new JFileChooser();
            }

            FileFilter filter = new FileNameExtensionFilter("*.png,*.jpg,*.jpeg", "png", "jpg", "jpeg");
            fileChooser.setFileFilter(filter);

            int approve = fileChooser.showOpenDialog(qrCodeForm.getQrCodePanel());
            if (approve == JFileChooser.APPROVE_OPTION) {
                qrCodeForm.getLogoPathTextField().setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        qrCodeForm.getSaveAsButton().addActionListener(e -> {
            File beforeFile = new File(App.config.getQrCodeSaveAsPath());
            JFileChooser fileChooser;
            if (beforeFile.exists()) {
                fileChooser = new JFileChooser(beforeFile);
            } else {
                fileChooser = new JFileChooser();
            }
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int approve = fileChooser.showOpenDialog(qrCodeForm.getQrCodePanel());
            String exportPath;
            if (approve == JFileChooser.APPROVE_OPTION) {
                exportPath = fileChooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }

            if (QrCodeForm.qrCodeImageTempFile.exists()) {
                FileUtil.copy(QrCodeForm.qrCodeImageTempFile.getAbsolutePath(), exportPath, true);
                JOptionPane.showMessageDialog(qrCodeForm.getQrCodePanel(), "保存成功！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                App.config.setQrCodeSaveAsPath(exportPath);
                App.config.save();
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(new File(exportPath));
                } catch (Exception e2) {
                    logger.error(e2);
                }
            }
        });
        qrCodeForm.getRecognitionExploreButton().addActionListener(e -> {
            File beforeFile = new File(qrCodeForm.getRecognitionImagePathTextField().getText());
            JFileChooser fileChooser;

            if (beforeFile.exists()) {
                fileChooser = new JFileChooser(beforeFile);
            } else {
                fileChooser = new JFileChooser();
            }

            FileFilter filter = new FileNameExtensionFilter("*.png,*.jpg,*.jpeg", "png", "jpg", "jpeg");
            fileChooser.setFileFilter(filter);

            int approve = fileChooser.showOpenDialog(qrCodeForm.getQrCodePanel());
            if (approve == JFileChooser.APPROVE_OPTION) {
                qrCodeForm.getRecognitionImagePathTextField().setText(fileChooser.getSelectedFile().getAbsolutePath());
                QrCodeForm.recognition();
            }
        });
        qrCodeForm.getRecognitionButton().addActionListener(e -> QrCodeForm.recognition());

        qrCodeForm.getFromClipBoardButton().addActionListener(e -> {
            QrCodeForm.recognitionFromClipBoard();
        });

        // 文本域按键事件
        qrCodeForm.getHistoryTextArea().addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent arg0) {
//                QrCodeForm.saveContent();
            }

            @Override
            public void keyPressed(KeyEvent evt) {
                if ((evt.isControlDown() || evt.isMetaDown()) && evt.getKeyCode() == KeyEvent.VK_S) {
                    QrCodeForm.saveContent();
                }
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        });
    }

    public static void output(String text) {
        QrCodeForm qrCodeForm = QrCodeForm.getInstance();
        ConsoleUtil.consoleOnly(qrCodeForm.getHistoryTextArea(), text);
        QrCodeForm.saveContent();
    }
}
