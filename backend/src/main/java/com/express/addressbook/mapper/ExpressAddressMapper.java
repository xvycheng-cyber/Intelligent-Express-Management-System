package com.express.addressbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.express.addressbook.entity.ExpressAddress;
import org.apache.ibatis.annotations.Param;

public interface ExpressAddressMapper extends BaseMapper<ExpressAddress> {

    /**
     * 关键词已由 LikeEscaper 转义；XML 使用 LIKE ... ESCAPE '!' 参数绑定。
     */
    IPage<ExpressAddress> selectPageByKeyword(
            Page<ExpressAddress> page,
            @Param("keywordPattern") String keywordPattern);
}
