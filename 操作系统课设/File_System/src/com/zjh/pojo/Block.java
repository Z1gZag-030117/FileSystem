package com.zjh.pojo;

import java.io.Serializable;

/**
 * @author �솴
 * @description: �����̿�
 */
public class Block implements Serializable {
    private int id; //���
    private int blockSize; //���С
    private String content; //������

    public Block(int id, int blockSize, String content) {
        this.id = id;
        this.blockSize = blockSize;
        this.content = content;
    }

    public Block() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + id +
                ", blockSize=" + blockSize +
                ", content='" + content + '\'' +
                '}';
    }
}
