package com.zjh.service.impl;

import com.zjh.constant.Constants;
import com.zjh.pojo.*;
import com.zjh.service.DirService;
import com.zjh.service.DiskService;
import com.zjh.utils.Utility;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * @author �솴
 * @description: ���̲���ʵ����
 * */
public class DiskServiceImpl implements DiskService {
    private static final DirService dirService = new DirServiceImpl();
    /**�ͷ�Ŀ¼�������ļ�ռ���ڴ棨����ջ��**/
    @Override
    public Boolean freeDir(FCB fcb) {
        Stack<FCB> fcbStack = new Stack<>();
        Queue<FCB> que = new LinkedList<>();
        List<FCB> children = fcb.getChildren();
        //��α�����ջ
        Boolean flag = false;
        while (!flag){
            //��ջ
            fcbStack.push(fcb);
            //�����
            que.offer(fcb);
        }
        //���γ�ջɾ��
        return null;
    }

    /**����ļ�ռ�ݵĴ��̿ռ估�ı�FAT��**/
    @Override
    public Boolean freeFile(FCB fcb) {
        FAT[] fats = Memory.getInstance().getFat();
        FAT temp1 = fats[fcb.getIndexNode().getFirst_block()];
        FAT temp2 = null;
        //1.�޸�FAT��
        while(temp1.getNextId() != -1){
            temp2 = temp1; //temp2��¼��ǰFAT���λ��
            temp1 = fats[temp1.getNextId()]; //temp1��¼FAT������һ����λ��
            //�Ͽ�ǰ������
            temp2.setNextId(-1);
            //��ռ�ݵ��̿��Ӧ�����ÿ�
            temp2.setBitmap(0);
        }
        temp1.setBitmap(0);
        //3.�ݹ��޸ĸ�Ŀ¼�ļ���С
        dirService.updateSize(fcb,false,-1);
        //4.��������С��Ϊ0 ���ļ�
        fcb.getIndexNode().setSize(0);
        return true;
    }

    /**������д����̿�**/
    @Override
    public int writeToDisk(String content) {
        //�ж��Ƿ����㹻�Ĵ��̿ռ�
        int needNum = Utility.ceilDivide(content.length(), Constants.BLOCK_SIZE);
        if(needNum > Memory.getInstance().getEmpty_blockNum()){
            System.out.println("[error]: ���̿ռ䲻�㣡");
            return -1;
        }
        //��ʼд��
        FAT[] fats = Memory.getInstance().getFat();
        int first = -1;
        //�ҵ���һ�����еĴ���
        first = find_empty();
        int temp1 = first;
        int temp2 = -1;
        Block[] disk = Disk.getINSTANCE().getDisk();
        int i = 0;
        for (; i < needNum - 1; i++) {
            String splitString = content.substring(i*Constants.BLOCK_SIZE,(i+1)*Constants.BLOCK_SIZE);
            //�洢������
            disk[temp1].setContent(splitString);
            fats[temp1].setBitmap(1);
            temp2 = temp1;
            //Ѱ����һ�����п�
            temp1 = find_empty();
            fats[temp2].setNextId(temp1);
        }
        //�������һ����
        disk[temp1].setContent(content.substring((i)*Constants.BLOCK_SIZE));
        fats[temp1].setNextId(-1);
        fats[temp1].setBitmap(1);
        //���ص�һ�����̿��
        return first;
    }

    /**Ѱ�ҿ��п�**/
    @Override
    public int find_empty() {
        FAT[] fats = Memory.getInstance().getFat();
        for (int i = 0; i < fats.length; i++) {
            if(fats[i].getBitmap() == 0){
                return i;
            }
        }
        return -1;
    }
}
