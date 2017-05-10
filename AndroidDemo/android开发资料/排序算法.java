public class 排序算法 {
	
	/**
	 * 冒泡排序----交换排序的一种

     * 方法：相邻两元素进行比较，如有需要则进行交换，每完成一次循环就将最大元素排在最后（如从小到大排序），
     * 下一次循环是将其他的数进行类似操作

     * 性能：比较次数O(n^2)；交换次数O(n^2)
	 */
	
	public void 冒泡排序(int[] data)
	{
		boolean flag = false;
		for (int i = 1; i < data.length && !flag; i++)
		{
			flag = true;//提前退出排序
			for (int j = 0; j < data.length - i; j++)
			{
				//从小到大排序
				if (data[j] > data[j + 1])
				{
					//交换
					data[j] 	= data[j] ^ data[j + 1];
					data[j + 1] = data[j] ^ data[j + 1];
					data[j] 	= data[j] ^ data[j + 1];
					flag = false;
				}
			}
		}
	}
	
	/**
	 * 直接选择排序法----选择排序的一种

     * 方法：每一趟从待排序的数据元素中选出最小（或最大）的一个元素，顺序放在已排好序的数列的最后，
     * 直到全部待排序的数据元素排完。

     * 性能：比较次数O(n^2)；交换次数O(n)
	 */
	
	public void 选择排序(int[] data)
	{
		for (int i = 0; i < data.length - 1; i++)
		{
			int index = i;
			for (int j = i + 1; j < data.length; j++)
			{
				//从小到大排序
				if (data[j] < data[index])
				{
					index = j;
				}
			}
			
			if (index != i)
			{
				//交换
				data[i] 	= data[i] ^ data[index];
				data[index] = data[i] ^ data[index];
				data[i] 	= data[i] ^ data[index];
			}
		}
	}
	
	/**
	 * 插入排序

     * 方法：将一个记录插入到已排好序的有序表（有可能是空表）中,从而得到一个新的记录数增1的有序表。

     * 性能：比较次数O(n^2)；交换次数O(n)
	 */
	
	public void 插入排序(int[] data)
	{
		for (int i = 1; i < data.length; i++)
		{
			for (int j = 0; j < i; j++)
			{
				//从小到大排序
				if (data[i] < data[j])
				{
					//交换
					data[i] = data[i] ^ data[j];
					data[j] = data[i] ^ data[j];
					data[i] = data[i] ^ data[j];
				}
			}
		}
	}
	
	public void 快速排序(int[] data)
	{
		quickSort(data, 0, data.length - 1);
	}
	
	private void quickSort(int[] data, int start, int end)
	{
		int i = start, j = end, mid = data[start];
		while (i < j)
		{
			while (i < j && data[j] >= mid)
				j--;
			data[i] = data[j];
			while (i < j && data[i] <= mid)
				i++;
			data[j] = data[i];
		}
		
		data[i] = mid;
		if (start < i - 1)
			quickSort(data, start, i - 1);
		if (end > i + 1)
			quickSort(data, i + 1, end);
	}
}