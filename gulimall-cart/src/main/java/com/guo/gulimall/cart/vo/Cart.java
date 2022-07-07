package com.guo.gulimall.cart.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 */
@Data
public class Cart {

    /**
     * 购物项列表
     */
    private List<CartItem> items;

    /**
     * 商品数量
     */
    private Integer countNum;

    /**
     * 商品类型数量
     */
    private Integer countType;

    /**
     * 商品总价
     */
    private BigDecimal totalAmount;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal(0);

    public List<CartItem> getItems() {
        return items;
    }

    public Integer getCountNum() {
        if (!CollectionUtils.isEmpty(this.items)) {
            this.countNum = this.items.stream().map(CartItem::getCount).reduce(0, Integer::sum);
        }
        return countNum;
    }

    public Integer getCountType() {
        if (!CollectionUtils.isEmpty(this.items)) {
            this.countType = this.items.stream().map(e -> 1).reduce(0, Integer::sum);
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal(0);
        if (!CollectionUtils.isEmpty(this.items)) {
            for (CartItem item : items) {
                amount = amount.add(item.getTotalPrice());
            }
        }
        amount = amount.subtract(this.getReduce());
        return amount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }
}
