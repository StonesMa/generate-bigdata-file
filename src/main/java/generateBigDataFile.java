import cn.hutool.core.io.FileUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class generateBigDataFile {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("未输入原始教据文件路径!");
        }
        if (args.length == 1) {
            System.out.println("未输入原始数据扩大倍教!");
        }
        //原始交件路径
        String inputFile = args[0];
        //扩大倍数
        Integer bigNum = Integer.valueOf(args[1]);

        String suffix = "." + FileUtil.getSuffix(inputFile);
        String inputFileNoHead = inputFile.replace(suffix, "-no-head" + suffix);

        //取出第一行的表头并明除后生成新文件
        String headLine = "";
        try {
            Scanner fileScanner = new Scanner(new File(inputFile));
            FileWriter fileStream = new FileWriter(inputFileNoHead);
            BufferedWriter out = new BufferedWriter(fileStream);
            int lineNum = 0;
            while (fileScanner.hasNextLine()) {
                if (lineNum == 0) {
                    headLine = fileScanner.nextLine() + FileUtil.getLineSeparator();
                    System.out.println("取出第一行且不复制到新交件");
                } else {
                    String next = fileScanner.nextLine();
                    if (next.equals("\n")) {
                        if (lineNum > 1) {
                            out.newLine();
                        }
                    } else {
                        if (lineNum > 1) {
                            out.newLine();
                        }
                        out.write(next);
                    }
                }
                lineNum++;
            }
            System.out.println("该数据文件共有" + lineNum + "行");
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String outputFile = inputFile.replace(suffix, "-big" + suffix);
        nioCopyFile(inputFileNoHead, outputFile, headLine, bigNum);
        new File(inputFileNoHead).delete();
    }

    private static void nioCopyFile(String inputFile, String outputFile, String headLine, Integer bigNum) {
        File inputFilePath = new File(inputFile);
        File outputFilePath = new File(outputFile);
        long startTime = System.currentTimeMillis();
        try {
            FileInputStream fileInputStream = new FileInputStream(inputFilePath);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
            FileChannel in = fileInputStream.getChannel();
            FileChannel out = fileOutputStream.getChannel();
            out.write(ByteBuffer.wrap(headLine.getBytes(StandardCharsets.UTF_8)));
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
            while ((in.read(byteBuffer)) != -1) {
                byteBuffer.flip();
                out.write(byteBuffer);
                byteBuffer.clear();
            }
            //两个文件内容之间换行
            out.write(ByteBuffer.wrap(FileUtil.getLineSeparator().getBytes(StandardCharsets.UTF_8)));
            for (int i = 0; i < bigNum; i++) {
                if (i > 0) {
                    //两个文件内容之间换行
                    out.write(ByteBuffer.wrap(FileUtil.getLineSeparator().getBytes(StandardCharsets.UTF_8)));
                }
                File file = new File(inputFile);
                FileInputStream fileInputStream2 = new FileInputStream(file);
                FileChannel in2 = fileInputStream2.getChannel();
                while (in2.read(byteBuffer) != -1) {
                    byteBuffer.flip();
                    out.write(byteBuffer);
                    byteBuffer.clear();
                }
                fileInputStream2.close();
                in2.close();
            }
            fileInputStream.close();
            fileOutputStream.close();
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("nio 复制文件用时：" + (endTime - startTime) + "ms");
    }

}