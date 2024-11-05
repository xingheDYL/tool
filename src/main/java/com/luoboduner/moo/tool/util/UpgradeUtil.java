package com.luoboduner.moo.tool.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.luoboduner.moo.tool.App;
import com.luoboduner.moo.tool.bean.VersionSummary;
import com.luoboduner.moo.tool.dao.TQuickNoteMapper;
import com.luoboduner.moo.tool.domain.TQuickNote;
import com.luoboduner.moo.tool.ui.UiConsts;
import com.luoboduner.moo.tool.ui.dialog.UpdateInfoDialog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 更新升级工具类
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">Zhou Bo</a>
 * @since 2019/5/24.
 */
@Slf4j
public class UpgradeUtil {
    private static TQuickNoteMapper quickNoteMapper = MybatisUtil.getSqlSession().getMapper(TQuickNoteMapper.class);

    public static void checkUpdate(boolean initCheck) {
        // 当前版本
        String currentVersion = UiConsts.APP_VERSION;

        // 从github获取最新版本相关信息
        String versionSummaryJsonContent = HttpUtil.get(UiConsts.CHECK_VERSION_URL);
        if (StringUtils.isEmpty(versionSummaryJsonContent) && !initCheck) {
            JOptionPane.showMessageDialog(App.mainFrame,
                    "检查超时，请关注GitHub Release！", "网络错误",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        } else if (StringUtils.isEmpty(versionSummaryJsonContent) || versionSummaryJsonContent.contains("404: Not Found")) {
            return;
        }
        versionSummaryJsonContent = versionSummaryJsonContent.replace("\n", "");

        VersionSummary versionSummary = JSON.parseObject(versionSummaryJsonContent, VersionSummary.class);
        // 最新版本
        String newVersion = versionSummary.getCurrentVersion();
        String versionIndexJsonContent = versionSummary.getVersionIndex();
        // 版本索引
        Map<String, String> versionIndexMap = JSON.parseObject(versionIndexJsonContent, Map.class);
        // 版本明细列表
        List<VersionSummary.Version> versionDetailList = versionSummary.getVersionDetailList();

        if (newVersion.compareTo(currentVersion) > 0) {
            // 当前版本索引
            int currentVersionIndex = Integer.parseInt(versionIndexMap.get(currentVersion));
            // 版本更新日志：
            StringBuilder versionLogBuilder = new StringBuilder("<h1>惊现新版本！立即下载？</h1>");
            VersionSummary.Version version;
            for (int i = currentVersionIndex + 1; i < versionDetailList.size(); i++) {
                version = versionDetailList.get(i);
                versionLogBuilder.append("<h2>").append(version.getVersion()).append("</h2>");
                versionLogBuilder.append("<b>").append(version.getTitle()).append("</b><br/>");
                versionLogBuilder.append("<p>").append(version.getLog().replaceAll("\\n", "</p><p>")).append("</p>");
            }
            String versionLog = versionLogBuilder.toString();

            UpdateInfoDialog updateInfoDialog = new UpdateInfoDialog();
            updateInfoDialog.setHtmlText(versionLog);
            updateInfoDialog.setNewVersion(newVersion);
            updateInfoDialog.pack();
            updateInfoDialog.setVisible(true);
        } else {
            if (!initCheck) {
                JOptionPane.showMessageDialog(App.mainFrame,
                        "当前已经是最新版本！", "恭喜",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * 平滑升级
     * 涉及的版本更新脚本和sql方法尽量幂等，以免升级过程中由于断电死机等异常中断造成重复执行升级操作
     */
    public static void smoothUpgrade() {
        // 取得当前版本
        String currentVersion = UiConsts.APP_VERSION;
        // 取得升级前版本
        String beforeVersion = App.config.getBeforeVersion();

        if (currentVersion.compareTo(beforeVersion) <= 0) {
            // 如果两者一致则不执行任何升级操作
            return;
        } else {
            log.info("平滑升级开始");
            // 否则先执行db_init.sql更新数据库新增表
            try {
                MybatisUtil.initDbFile();
            } catch (Exception e) {
                log.error("执行平滑升级时先执行db_init.sql操作失败", e);
                return;
            }

            // 然后取两个版本对应的索引
            String versionSummaryJsonContent = FileUtil.readString(UiConsts.class.getResource("/version_summary.json"), CharsetUtil.UTF_8);
            versionSummaryJsonContent = versionSummaryJsonContent.replace("\n", "");
            VersionSummary versionSummary = JSON.parseObject(versionSummaryJsonContent, VersionSummary.class);
            String versionIndex = versionSummary.getVersionIndex();
            Map<String, String> versionIndexMap = JSON.parseObject(versionIndex, Map.class);
            int currentVersionIndex = Integer.parseInt(versionIndexMap.get(currentVersion));
            int beforeVersionIndex = Integer.parseInt(versionIndexMap.get(beforeVersion));
            log.info("旧版本{}", beforeVersion);
            log.info("当前版本{}", currentVersion);
            // 遍历索引范围
            beforeVersionIndex++;
            for (int i = beforeVersionIndex; i <= currentVersionIndex; i++) {
                log.info("更新版本索引{}开始", i);
                // 执行每个版本索引的更新内容，按时间由远到近
                // 取得resources:upgrade下对应版本的sql，如存在，则先执行sql进行表结构或者数据更新等操作
                String sqlFile = "/upgrade/" + i + ".sql";
                URL sqlFileUrl = UiConsts.class.getResource(sqlFile);
                if (sqlFileUrl != null) {
                    String sql = FileUtil.readString(sqlFileUrl, CharsetUtil.UTF_8);
                    try {
                        MybatisUtil.executeSql(sql);
                    } catch (SQLException e) {
                        log.error("执行索引为{}的版本对应的sql时异常", i, e);
                        if (!e.getMessage().contains("duplicate column") && !e.getMessage().contains("constraint")) {
                            return;
                        }
                    }
                }
                upgrade(i);
                log.info("更新版本索引{}结束", i);
            }

            // 升级完毕且成功，则赋值升级前版本号为当前版本
            App.config.setBeforeVersion(currentVersion);
            App.config.save();
            log.info("平滑升级结束");
        }
    }

    /**
     * 执行升级脚本
     *
     * @param versionIndex 版本索引
     */
    private static void upgrade(int versionIndex) {
        log.info("执行升级脚本开始，版本索引：{}", versionIndex);
        switch (versionIndex) {
            case 12:
                // 初始化随手记表中的字体和语法数据
                TQuickNote tQuickNote = new TQuickNote();
                tQuickNote.setSyntax(SyntaxConstants.SYNTAX_STYLE_NONE);
                tQuickNote.setFontName(App.config.getQuickNoteFontName());
                tQuickNote.setFontSize(String.valueOf(App.config.getFontSize()));
                quickNoteMapper.updateAll(tQuickNote);
            case 21:
                break;
            default:
        }
        log.info("执行升级脚本结束，版本索引：{}", versionIndex);
    }
}
