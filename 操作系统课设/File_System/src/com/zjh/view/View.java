package com.zjh.view;

import com.zjh.pojo.FCB;
import com.zjh.pojo.IndexNode;
import com.zjh.utils.Utility;

/**
 * @author �솴
 * @description: չʾ������
 */
public class View {
    //�����ĵ�
    public void help(){
        System.out.println("=====command======");
        System.out.println("<login> ��¼");
        System.out.println("<register> ע��");
        System.out.println("<logout> �˳�");
        System.out.println("<mkdir> [dirName] ����Ŀ¼");
        System.out.println("<create> [fileName] �����ļ�");
        System.out.println("<cd> [fileName] �л���ָ��Ŀ¼");
        System.out.println("<open> [fileName] ���ļ�");
        System.out.println("<close> [fileName] �ر��ļ�");
        System.out.println("<read> [fileName] ���ļ�");
        System.out.println("<write> [fileName] д�ļ�");
        System.out.println("<delete> [fileName] ɾ���ļ�");
        System.out.println("<show_open> ��ʾ�򿪵��ļ�");
        System.out.println("<bitmap> ��ʾλʾͼ");
        System.out.println("<ls> ��ʾĿ¼�ļ���");
        System.out.println("<rename> [filePath] [newName]�ļ�������");
        System.out.println("<dir/ll> ��ʾ��ǰĿ¼�������ļ���ϸ��Ϣ");
        System.out.println("=====command======");
    }
    public void showFcb(FCB fcb,int color){
        IndexNode indexNode = fcb.getIndexNode();
        System.out.printf("%-1s%-6s\t  %-2d\t  %-5s\t  %-3d\t%28s\t%-8s",
                fcb.getType(),
                indexNode.getPermission(),
                indexNode.getFcbNum(),
                indexNode.getCreator(),
                indexNode.getSize(),
                indexNode.getUpdateTime(),
                color == -1? fcb.getFileName() : Utility.getFormatLogString(fcb.getFileName(),color,0));
        System.out.println();
    }
}
