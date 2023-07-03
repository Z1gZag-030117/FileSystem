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
 * @author 朱喆
 * @description: 磁盘操作实现类
 * */
public class DiskServiceImpl implements DiskService {
    private static final DirService dirService = new DirServiceImpl();
    /**释放目录中所有文件占用内存（借助栈）**/
    @Override
    public Boolean freeDir(FCB fcb) {
        Stack<FCB> fcbStack = new Stack<>();
        Queue<FCB> que = new LinkedList<>();
        List<FCB> children = fcb.getChildren();
        //层次遍历入栈
        Boolean flag = false;
        while (!flag){
            //入栈
            fcbStack.push(fcb);
            //入队列
            que.offer(fcb);
        }
        //依次出栈删除
        return null;
    }

    /**清除文件占据的磁盘空间及改变FAT表**/
    @Override
    public Boolean freeFile(FCB fcb) {
        FAT[] fats = Memory.getInstance().getFat();
        FAT temp1 = fats[fcb.getIndexNode().getFirst_block()];
        FAT temp2 = null;
        //1.修改FAT表
        while(temp1.getNextId() != -1){
            temp2 = temp1; //temp2记录当前FAT表的位置
            temp1 = fats[temp1.getNextId()]; //temp1记录FAT表中下一个的位置
            //断开前后连接
            temp2.setNextId(-1);
            //将占据的盘块对应内容置空
            temp2.setBitmap(0);
        }
        temp1.setBitmap(0);
        //3.递归修改父目录文件大小
        dirService.updateSize(fcb,false,-1);
        //4.索引结点大小变为0 空文件
        fcb.getIndexNode().setSize(0);
        return true;
    }

    /**将内容写入磁盘块**/
    @Override
    public int writeToDisk(String content) {
        //判断是否有足够的磁盘空间
        int needNum = Utility.ceilDivide(content.length(), Constants.BLOCK_SIZE);
        if(needNum > Memory.getInstance().getEmpty_blockNum()){
            System.out.println("[error]: 磁盘空间不足！");
            return -1;
        }
        //开始写入
        FAT[] fats = Memory.getInstance().getFat();
        int first = -1;
        //找到第一个空闲的磁盘
        first = find_empty();
        int temp1 = first;
        int temp2 = -1;
        Block[] disk = Disk.getINSTANCE().getDisk();
        int i = 0;
        for (; i < needNum - 1; i++) {
            String splitString = content.substring(i*Constants.BLOCK_SIZE,(i+1)*Constants.BLOCK_SIZE);
            //存储到磁盘
            disk[temp1].setContent(splitString);
            fats[temp1].setBitmap(1);
            temp2 = temp1;
            //寻找下一个空闲块
            temp1 = find_empty();
            fats[temp2].setNextId(temp1);
        }
        //设置最后一个块
        disk[temp1].setContent(content.substring((i)*Constants.BLOCK_SIZE));
        fats[temp1].setNextId(-1);
        fats[temp1].setBitmap(1);
        //返回第一个磁盘块号
        return first;
    }

    /**寻找空闲块**/
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
