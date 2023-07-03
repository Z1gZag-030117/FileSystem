package com.zjh.utils;

/**
 * @author �솴
 * @description: ������
 */
public class Utility {
    /**
     * ����ȡ���ĳ��� ���ڷ����̿�
     * @param dividend ������
     * @param divisor ����
     * @return ���� (dividend / divisor) ������ȡ�����
     */
    public static int ceilDivide(int dividend,int divisor) {
        if (dividend % divisor == 0) {
            return dividend / divisor;// ����
        } else {
            return (dividend + divisor) / divisor;// ������������ȡ��
        }
    }

    /**
     * �ж��ַ����Ƿ�Ϊ�գ������Ƿ�ȫ�ǿո��
     * @param str �ַ���
     * @return true-Ϊ�գ�����ȫ�ǿո��
     */
    public static boolean isAllSpace(String str) {
        return (str == null || "".equals(str.trim()));
    }

    /**
     * �����û����������
     * @param input �û�������
     * @return �������
     */
    public static String[] inputResolve(String input) {
        if (Utility.isAllSpace(input)) {
            return new String[]{""};
        }
        return input.trim().split("\\s+"); //ƥ�����ո�
    }

    /**
     * ��������̨���������ɫ
     * @param content ����
     * @param colour  ��ɫ
     * @param type    ����
     * @return {@link String}
     */
    public static String getFormatLogString(String content, int colour, int type) {
        boolean hasType = type != 1 && type != 3 && type != 4;
        if (hasType) {
            return String.format("\033[%dm%s\033[0m", colour, content);
        } else {
            return String.format("\033[%d;%dm%s\033[0m", colour, type, content);
        }
    }

    /**
     * ��ȡ�ļ��ĺ�׺��
     * @param fileName �ļ���
     * @return ��׺��
     */
    public static String getSuffix(String fileName) {
        if (fileName == null || "".equals(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf(".");
        if (index == fileName.length()) {
            return "";
        }
        return fileName.substring(index + 1, fileName.length());
    }

}
