package com.luoboduner.moo.tool.ui.component.textviewer;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.util.FontUtils;
import com.formdev.flatlaf.util.StringUtils;
import com.luoboduner.moo.tool.App;
import com.luoboduner.moo.tool.ui.listener.func.QuickNoteListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextArea;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class QuickNoteRSyntaxTextViewer extends RSyntaxTextArea {
    public static boolean ignoreQuickSave;

    public QuickNoteRSyntaxTextViewer() {

//        setUseSelectedTextColor(true);
//        setSelectedTextColor(new Color(50, 50, 50));

//        setSelectionColor(timeHisTextArea.getSelectionColor());
//        setCaretColor(UIManager.getColor("Editor.caretColor"));
//        setMarkAllHighlightColor(UIManager.getColor("Editor.markAllHighlightColor"));
//        setMarkOccurrencesColor(UIManager.getColor("Editor.markOccurrencesColor"));
//        setMatchedBracketBGColor(UIManager.getColor("Editor.matchedBracketBackground"));
//        setMatchedBracketBorderColor(UIManager.getColor("Editor.matchedBracketBorderColor"));

        updateTheme();

//        Font font = createEditorFont( 0 );
//        setFont( font );

        setHyperlinksEnabled(true);
        addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(e.getURL().toString()));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setDoubleBuffered(true);

        // 文本域键盘事件
        addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent evt) {
                if ((evt.isControlDown() || evt.isMetaDown()) && evt.getKeyCode() == KeyEvent.VK_S) {
                    QuickNoteListener.quickSave(true, true);
                } else if (((evt.isControlDown() || evt.isMetaDown()) && evt.isShiftDown() && evt.getKeyCode() == KeyEvent.VK_F)
                        || evt.isMetaDown() && evt.isAltDown() && evt.getKeyCode() == KeyEvent.VK_L) {
                    QuickNoteListener.format();
                } else if ((evt.isControlDown() || evt.isMetaDown()) && evt.getKeyCode() == KeyEvent.VK_F) {
                    QuickNoteListener.showFindPanel();
                } else if ((evt.isControlDown() || evt.isMetaDown()) && evt.getKeyCode() == KeyEvent.VK_R) {
                    QuickNoteListener.showFindPanel();
                } else if ((evt.isControlDown() || evt.isMetaDown()) && evt.getKeyCode() == KeyEvent.VK_N) {
                    QuickNoteListener.newNote();
                }
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        });

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (ignoreQuickSave) {
                    return;
                }
                QuickNoteListener.quickSave(true, false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (ignoreQuickSave) {
                    return;
                }
                QuickNoteListener.quickSave(true, false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (ignoreQuickSave) {
                    return;
                }
                QuickNoteListener.quickSave(true, false);
            }
        });
    }

    public void updateTheme() {
        try {
            Theme theme;
            if (FlatLaf.isLafDark()) {
                theme = Theme.load(App.class.getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
            } else {
                theme = Theme.load(App.class.getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
            }
            theme.apply(this);
        } catch (IOException ioe) { // Never happens
            ioe.printStackTrace();
        }

        if (StringUtils.isEmpty(getSyntaxEditingStyle())) {
            setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }
        setCodeFoldingEnabled(true);

        setBackground(UIManager.getColor("Editor.background"));
        setCaretColor(UIManager.getColor("Editor.caretColor"));
        setSelectionColor(UIManager.getColor("Editor.selectionBackground"));
        setCurrentLineHighlightColor(UIManager.getColor("Editor.currentLineHighlight"));
        setMarkAllHighlightColor(UIManager.getColor("Editor.markAllHighlightColor"));
        setMarkOccurrencesColor(UIManager.getColor("Editor.markOccurrencesColor"));
        setMatchedBracketBGColor(UIManager.getColor("Editor.matchedBracketBackground"));
        setMatchedBracketBorderColor(UIManager.getColor("Editor.matchedBracketBorderColor"));
        setPaintMatchedBracketPair(true);
        setAnimateBracketMatching(false);

//        // 初始化背景色
////        Style.blackTextArea(this);
//        setBackground(TimeConvertForm.getInstance().getTimeHisTextArea().getBackground());
//        // 初始化边距
//        setMargin(new Insets(10, 10, 10, 10));
//
        // 初始化字体
        String fontName = App.config.getQuickNoteFontName();
        int fontSize = App.config.getQuickNoteFontSize();
        if (fontSize == 0) {
            fontSize = getFont().getSize() + 2;
        }
        Font font;
        if (FlatJetBrainsMonoFont.FAMILY.equals(fontName)) {
            font = FontUtils.getCompositeFont(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, fontSize);
        } else if (FlatInterFont.FAMILY.equals(fontName)) {
            font = FontUtils.getCompositeFont(FlatInterFont.FAMILY, Font.PLAIN, fontSize);
        } else {
            font = new Font(fontName, Font.PLAIN, fontSize);
        }
        setFont(font);
    }

    private static Font createEditorFont(int sizeIncr) {
        int size = UIManager.getFont("defaultFont").getSize() + sizeIncr;
        Font font = FontUtils.getCompositeFont(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, size);
        if (isFallbackFont(font)) {
            Font defaultFont = RTextArea.getDefaultFont();
            font = defaultFont.deriveFont((float) size);
        }
        return font;
    }

    private static boolean isFallbackFont(Font font) {
        return Font.DIALOG.equalsIgnoreCase(font.getFamily());
    }
}
