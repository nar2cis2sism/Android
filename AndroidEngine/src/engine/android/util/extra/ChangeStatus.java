package engine.android.util.extra;

import engine.android.util.io.ByteDataUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * 功能：记录对象属性的修改状态
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class ChangeStatus {
    
    private final HashMap<String, Integer> bitMap = new HashMap<String, Integer>();
    
    private final byte[] data;
    
    private int bit = 1;
    
    public ChangeStatus() {
        data = ByteDataUtil.intToBytes_HL(0);
    }
    
    /**
     * 设置属性的修改状态
     */
    public void setChanged(String property, boolean change) {
        int bit = getBitForKey(property);
        int index = getDataIndex(bit);
        if (index >= data.length)
        {
            throw new IllegalStateException("The entire number of property is more than 32.");
        }
        
        data[index] = ByteDataUtil.setBitMask(data[index], getLocation(bit), change);
    }
    
    /**
     * 获取属性的修改状态
     */
    public boolean isChanged(String property) {
        if (!bitMap.containsKey(property))
        {
            return false;
        }
        
        return hasBitMask(getBitForKey(property));
    }

    /**
     * 判断对象是否有修改属性
     */
    public boolean isChanged() {
        return ByteDataUtil.bytesToInt_HL(data, 0) != 0;
    }
    
    /**
     * 获取修改的属性数组
     */
    public String[] getChangedProperties() {
        ArrayList<String> list = new ArrayList<String>(bitMap.size());
        for (Entry<String, Integer> entry : bitMap.entrySet())
        {
            if (hasBitMask(entry.getValue()))
            {
                list.add(entry.getKey());
            }
        }
        
        return list.toArray(new String[list.size()]);
    }
    
    private boolean hasBitMask(int bit) {
        return ByteDataUtil.hasBitMask(data[getDataIndex(bit)], getLocation(bit));
    }
    
    /**
     * 为每一条属性分配一个bit
     */
    private int getBitForKey(String property) {
        Integer bit = bitMap.get(property);
        if (bit == null)
        {
            bitMap.put(property, bit = this.bit++);
        }
        
        return bit;
    }
    
    /**
     * 取得要操作的数据索引
     */
    private int getDataIndex(int bit) {
        return (bit - 1) / Byte.SIZE;
    }
    
    /**
     * 取得要操作的位移
     */
    private int getLocation(int bit) {
        return (bit - 1) % Byte.SIZE + 1;
    }
}