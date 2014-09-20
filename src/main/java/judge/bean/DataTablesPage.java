package judge.bean;


import java.util.List;

import org.apache.struts2.json.annotations.JSON;

@SuppressWarnings("unchecked")
public class DataTablesPage {

    private List aaData;
    private Long iTotalRecords;
    private Long iTotalDisplayRecords;
    private String sColumns;

    @JSON(name = "iTotalRecords")
    public Long getITotalRecords() {
        return iTotalRecords;
    }

    public void setITotalRecords(Long totalRecords) {
        iTotalRecords = totalRecords;
    }

    @JSON(name = "iTotalDisplayRecords")
    public Long getITotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setITotalDisplayRecords(Long totalDisplayRecords) {
        iTotalDisplayRecords = totalDisplayRecords;
    }

    @JSON(name = "sColumns")
    public String getSColumns() {
        return sColumns;
    }

    public void setSColumns(String columns) {
        sColumns = columns;
    }

    @JSON(name = "aaData")
    public List getAaData() {
        return aaData;
    }

    public void setAaData(List aaData) {
        this.aaData = aaData;
    }



}
