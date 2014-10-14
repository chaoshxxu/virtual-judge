package judge.bean;


import java.util.List;

import org.apache.struts2.json.annotations.JSON;

public class DataTablesPage {

    private List data;
    private Long recordsTotal;
    private Long recordsFiltered;
    private Integer draw;

    
    @JSON(name = "recordsTotal")
    public Long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    @JSON(name = "recordsFiltered")
    public Long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    @JSON(name = "data")
    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public Integer getDraw() {
        return draw;
    }

    public void setDraw(Integer draw) {
        this.draw = draw;
    }
    

}
