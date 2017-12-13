package engine.android.dao.util;

/**
 * 分页工具（辅助类）
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class Page {

    private int firstPage = 1;
    private int lastPage = -1;
    private boolean startFromZero;

    private int totalPage;                      // 总页数
    private int currentPage;                    // 当前页数(default start from 1)

    private int totalRecord;                    // 总记录条数

    private int beginRecord;                    // 起始记录索引(included)
    private int endRecord;                      // 结束记录索引(excluded)

    private int pageSize;                       // 每页显示的记录数

    /**
     * @param pageSize Must be > 0
     */
    public Page(int pageSize) {
        this(pageSize, 0);
    }

    public Page(int pageSize, int totalRecord) {
        checkPageSize(pageSize);
        this.pageSize = pageSize;
        setTotalRecord(totalRecord);
    }

    private void checkPageSize(int pageSize) {
        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("page size must be > 0");
        }
    }

    @Override
    public String toString() {
        return String.format("page:%s/%s record:%s-%s/%s",
                currentPage, totalPage, beginRecord, endRecord - 1, totalRecord);
    }

    public int getTotalPage() {
        return totalPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalRecord() {
        return totalRecord;
    }

    public int getBeginRecord() {
        return beginRecord;
    }

    public int getEndRecord() {
        return endRecord;
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param startFromZero True:起始页从0开始 False:起始页从1开始
     */
    public void switchStartFromZero(boolean startFromZero) {
        if (this.startFromZero != startFromZero)
        {
            updateLastPage(firstPage = (this.startFromZero = startFromZero) ? 0 : 1);
        }
    }

    private void updateLastPage(int firstPage) {
        lastPage = firstPage + totalPage - 1;
        setCurrentPage(currentPage);
    }

    /**
     * 设置总页数
     * 
     * @param totalRecord 总记录条数
     * @param pageSize 每页显示的记录数
     */
    private void setTotalPage(int totalRecord, int pageSize) {
        if (totalRecord % pageSize == 0)
        {
            totalPage = totalRecord / pageSize;
        }
        else
        {
            totalPage = totalRecord / pageSize + 1;
        }

        updateLastPage(firstPage);
    }

    private void setCurrentPage(int currentPage, int pageSize) {
        endRecord = Math.min(totalRecord, (beginRecord = ((this.currentPage = currentPage)
                - firstPage) * pageSize) + pageSize);
    }

    public void setCurrentPage(int currentPage) {
        setCurrentPage(Math.max(firstPage, Math.min(lastPage, currentPage)), pageSize);
    }

    public void setTotalRecord(int totalRecord) {
        if (this.totalRecord != totalRecord)
        {
            setTotalPage(this.totalRecord = totalRecord, pageSize);
        }
    }

    public void setCurrentRecord(int currentRecord) {
        setCurrentPage(currentRecord / pageSize + firstPage);
    }

    public void setPageSize(int pageSize) {
        if (this.pageSize != pageSize)
        {
            checkPageSize(pageSize);
            setTotalPage(totalRecord, this.pageSize = pageSize);
        }
    }

    public boolean hasPreviousPage() {
        return currentPage > firstPage;
    }

    /**
     * 上翻页
     */
    public void previousPage() {
        setCurrentPage(currentPage - 1);
    }

    public boolean hasNextPage() {
        return currentPage < lastPage;
    }

    /**
     * 下翻页
     */
    public void nextPage() {
        setCurrentPage(currentPage + 1);
    }

    public boolean isFirstPage() {
        return currentPage == firstPage;
    }

    public void jumpToFirstPage() {
        setCurrentPage(firstPage, pageSize);
    }

    public boolean isLastPage() {
        return currentPage == lastPage;
    }

    public void jumpToLastPage() {
        setCurrentPage(lastPage, pageSize);
    }

    public boolean hasRecord() {
        return totalRecord > 0;
    }

    public boolean hasOnlyOnePage() {
        return firstPage == lastPage;
    }
}