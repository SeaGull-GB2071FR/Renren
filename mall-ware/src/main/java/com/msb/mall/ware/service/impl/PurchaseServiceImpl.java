package com.msb.mall.ware.service.impl;

import com.msb.common.constant.WareConstant;
import com.msb.mall.ware.entity.PurchaseDetailEntity;
import com.msb.mall.ware.service.PurchaseDetailService;
import com.msb.mall.ware.vo.MergeVO;
import com.msb.mall.ware.vo.PurchaseDoneVO;
import com.msb.mall.ware.vo.PurchaseItemDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.ware.dao.PurchaseDao;
import com.msb.mall.ware.entity.PurchaseEntity;
import com.msb.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {


    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询采购单的状态为 新建 或者 已分配 的采购单信息
     *
     * @param params
     * @return
     */

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).or().like("status", 1);
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), wrapper);


        return new PageUtils(page);
    }

    /**
     * 完成采购需求的合单操作
     *
     * @param mergeVO
     * @return
     */
    @Transactional
    @Override
    public Integer merge(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        // 如果id 为 null
        if (purchaseId == null) {
            // 新建 采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // 整合菜单需求单
        List<Long> items = mergeVO.getItems();
        final long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> list = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);

            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(list);

        // 更新对应的采购单的更新时间
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchaseId);
        entity.setUpdateTime(new Date());
        this.updateById(entity);
        return null;
    }

    /**
     * 领取采购单
     * todo
     *
     * @param ids
     */
    @Transactional
    @Override
    public void received(List<Long> ids) {

        // 1.领取的采购单的状态只能是新建或者已分配的采购单 其他的是不能领取的
        List<PurchaseEntity> list = ids.stream().map(this::getById).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGED.getCode();
        }).map(item -> {
            item.setUpdateTime(new Date()); // 更新修改时间
            // 更新采购单的状态为 已领取
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());

            return item;
        }).collect(Collectors.toList());

        // 2.更新采购单的状态为 已领取
        this.updateBatchById(list);

        // 3.更新采购项的状态为 正在采购
        // 遍历id列表
        for (Long id : ids) {
            // 根据id查询采购项列表
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(id);

            // 遍历采购项列表，创建新的采购项实体，并将状态设置为“正在购买”
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailEntities.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(id);

                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return
                        purchaseDetailEntity;
            }).collect(Collectors.toList());

            // 批量更新采购项
            purchaseDetailService.updateBatchById(purchaseDetailEntityList);

        }

    }

    //todo
    @Transactional
    @Override
    public void done(PurchaseDoneVO vo) {
        // 获取采购单编号
        Long id = vo.getId();
        // 2.改变采购项的状态
        Boolean flag = true; // 记录采购的状态 默认为 完成
        // 获取所有的采购项
        List<PurchaseItemDoneVO> items = vo.getItems();
        List<PurchaseDetailEntity> list = new ArrayList<>();
        for (PurchaseItemDoneVO item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            // 该采购项采购出现了问题
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.HASERROR.getCode()) { 
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                item.setStatus(WareConstant.PurchaseStatusEnum.FINISH.getCode());
                // 将采购成功的采购项进入库操作
                // 跟进采购项编号查询出对应的采购项详情
                PurchaseDetailEntity detailEntity1 = purchaseDetailService.getById(item.getItemId());
                purchaseDetailService.addStock(detailEntity1.getSkuId(), detailEntity1.getWareId(), detailEntity1.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            //detailService.updateById(detailEntity);
            list.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(list); // 批量更新 采购项

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode()
                : WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}