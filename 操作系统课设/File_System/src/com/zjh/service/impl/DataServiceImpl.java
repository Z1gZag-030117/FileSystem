package com.zjh.service.impl;

import com.zjh.constant.Constants;
import com.zjh.pojo.*;
import com.zjh.pojo.Disk;
import com.zjh.pojo.Memory;
import com.zjh.service.DataService;

import java.io.*;
import java.util.*;

/**
 * @author �솴
 * @description: �־û��ӿ�ʵ����
 */
public class DataServiceImpl implements DataService {
    @Override
    public void init() {
        Disk newDisk = Disk.getINSTANCE();
        Block[] disk = new Block[Constants.BLOCK_COUNT];
        //��ʼ������
        for (int i = 0; i < Constants.BLOCK_COUNT; i++) {
            disk[i] = new Block();
            disk[i].setId(i);
            disk[i].setBlockSize(Constants.BLOCK_SIZE);
            disk[i].setContent(null);
        }
        newDisk.setDisk(disk);
        //��ʼ��FCB����
        List<FCB> fcbList = new ArrayList<>();
        newDisk.setFcbList(fcbList);
        //��ʼ����Ŀ¼
        IndexNode indexNode = new IndexNode("rwxrwx",0,-1,0,null,new Date());
        FCB rootDir = new FCB("rootDir",'D',indexNode,null,new LinkedList<>());
        fcbList.add(rootDir);

        //��ʼ��FAT��
        FAT[] fats = new FAT[Constants.BLOCK_COUNT];
        for (int i = 0; i < Constants.BLOCK_COUNT; i++) {
            fats[i] = new FAT();
            fats[i].setId(i);
            fats[i].setBitmap(0);
            fats[i].setNextId(-1);
        }
        newDisk.setFat(fats);
        //��ʼ���ڴ�
        Memory memory = Memory.getInstance();
        //�û�����
        Map<String, User> userMap = new HashMap<>();
        newDisk.setUserMap(userMap);
        //��ʼ���ڴ�
        memory.setUserMap(userMap);
        memory.setCurDir(rootDir);
        memory.setCurUser(null);
        memory.setRootDir(rootDir);
        memory.setFat(fats);
        ArrayList<OpenFile> openFiles = new ArrayList<>();
        memory.setOpenFileList(openFiles);
        memory.setEmpty_blockNum(Constants.BLOCK_COUNT);
        System.out.println("[success]��ʼ���ɹ�");
    }

    @Override
    public Boolean loadData(String dataPath) {
        File file = new File(dataPath);
        if (!file.exists()) {
            System.out.println("[error]:�Ҳ����ļ�");
            return false;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            //���ش�������
            Disk.setINSTANCE((Disk) ois.readObject());
            Disk instance = Disk.getINSTANCE();
            //���������ݵ����ڴ�
            Memory memory = Memory.getInstance();
            memory.setUserMap(instance.getUserMap());
            memory.setCurUser(null);
            memory.setRootDir(instance.getFcbList().get(0));
            memory.setCurDir(instance.getFcbList().get(0));
            FAT[] fats = instance.getFat();
            memory.setFat(fats);
            List<OpenFile> openFileList = new ArrayList<>();
            memory.setOpenFileList(openFileList);
            int empty_blockNum = 0;
            for (int i = 0; i < fats.length; i++) {
                if(fats[i].getBitmap() == 0){
                    empty_blockNum++;
                }
            }
            memory.setEmpty_blockNum(empty_blockNum);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[error]:IO�쳣");
            return false;
        }finally {
            try {
                if (Objects.nonNull(ois)) {
                    ois.close();
                }
            } catch (IOException ignored) {
            }
        }
        System.out.println("[success]�������ݳɹ�");
        return true;
    }

    @Override
    public Boolean saveData(String savePath) {
        File file = new File(savePath);
        // ����ļ��Ƿ����
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                if (new File(file.getParentFile().getPath()).mkdirs()) {
                    try {
                        if (!file.createNewFile()) {
                            System.out.println("[error]:����ʧ��");
                            return false;
                        }
                    } catch (IOException ioException) {
                        System.out.println("[error]:IO�쳣");
                        return false;
                    }
                } else {
                    System.out.println("[error]:����ʧ��");
                    return false;
                }
            }
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            // �־û��������ļ���
            oos.writeObject(Disk.getINSTANCE());
            oos.flush();
            System.out.println("[success]�������ݳɹ�");
            return true;
        } catch (IOException e) {
            System.out.println("[error]:����ʧ��");
            return false;
        } finally {
            try {
                if (Objects.nonNull(oos)) {
                    oos.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
