package com.github.zi_jing.aabbcalculator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final String VERSION = "1.0.0";
    public static boolean firstStart = false;
    private static int blockSize = 16;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        log("");
        log("================================");
        log("  AABBCalculator 版本 " + VERSION);
        log("================================");
        start:
        while (true) {
            List<String> results = new ArrayList<>();
            log("\n将模型json文件拖入命令行窗口，按回车键确认，输入\"exit\"退出。");
            log("默认情况下完整正方体方块的大小为16*16*16，如果想改为其他大小请输入\";size <大小>\"并按回车。示例：;size 32");
            log("请输入文件路径或拖入文件，可以使用分号(\";\")分隔多个路径: ");
            String input = scanner.nextLine();
            if (input.equals("exit"))
                break;
            if (input.startsWith(";size")) {
                String[] fragments = StringUtils.split(input, ' ');
                int size;
                try {
                    size = Integer.parseInt(fragments[1]);
                    if (size <= 0) {
                        log("无效大小，方块大小必须大于0");
                        continue;
                    }
                    blockSize = size;
                } catch (Exception e) {
                    log("输入语法有误，请仔细查看提示后重试");
                    continue;
                }
                log("设置成功！该设置会一直保留到你退出程序时为止，如需修改可以再次执行该命令。");
                continue;
            }
            String[] paths = StringUtils.split(input, ';');
            if (paths.length == 0) {
                log("输入无效，请重新输入");
                continue;
            }
            for (String path : paths) {
                File file = new File(path);
                if (!file.exists() || !file.isFile()) {
                    log("文件 " + path + " 不存在！");
                    continue start;
                }
                log("正在处理文件 " + path);
                JsonParser parser = new JsonParser();
                String jsonString;
                try {
                    jsonString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log("无法读取 " + path + " !");
                    e.printStackTrace();
                    continue start;
                }
                JsonObject modelJson;
                try {
                    modelJson = parser.parse(jsonString).getAsJsonObject();
                } catch (Exception e) {
                    log("Json语法错误：" + path);
                    e.printStackTrace();
                    continue start;
                }
                if (!modelJson.has("elements")) {
                    log("模型错误：找不到\"elements\"成员：" + path);
                    continue start;
                }
                JsonElement elements = modelJson.get("elements");
                if (!elements.isJsonArray()) {
                    log("模型错误：\"elements\"成员必须为数组：" + path);
                    continue start;
                }
                JsonArray elementsArray = elements.getAsJsonArray();
                try {
                    int startX = Integer.MAX_VALUE;
                    int startY = Integer.MAX_VALUE;
                    int startZ = Integer.MAX_VALUE;
                    int endX = 0;
                    int endY = 0;
                    int endZ = 0;
                    for (JsonElement e : elementsArray) {
                        JsonArray from = e.getAsJsonObject().getAsJsonArray("from");
                        JsonArray to = e.getAsJsonObject().getAsJsonArray("to");
                        if (from.get(0).getAsInt() < startX) startX = from.get(0).getAsInt();
                        if (from.get(1).getAsInt() < startY) startY = from.get(1).getAsInt();
                        if (from.get(2).getAsInt() < startZ) startZ = from.get(2).getAsInt();
                        if (to.get(0).getAsInt() > endX) endX = to.get(0).getAsInt();
                        if (to.get(1).getAsInt() > endY) endY = to.get(1).getAsInt();
                        if (to.get(2).getAsInt() > endZ) endZ = to.get(2).getAsInt();
                    }
                    results.add(String.format("new AxisAlignedBB(%fD, %fD, %fD, %fD, %fD, %fD);", ((double) startX / blockSize), ((double) startY / blockSize), ((double) startZ / blockSize), ((double) endX / blockSize), ((double) endY / blockSize), ((double) endZ / blockSize)));
                } catch (Exception e) {
                    log("模型语法错误：" + path);
                    e.printStackTrace();
                    continue start;
                }
            }
            log("");
            log("正在生成代码...");
            log("");
            for (String s : results)
                log(s);
            log("");
            log("完成！\n");
        }
        scanner.close();
    }

    private static void log(String msg) {
        System.out.println(msg);
    }
}
