package com.guo.common.to.mq;


import lombok.Data;

@Data
public class StockLockedTO {

    /**
     * 库存工作单的id
     */
    private Long taskId;

    /**
     * 工作单详情 Id
     */
    private Long taskDetailId;
}
