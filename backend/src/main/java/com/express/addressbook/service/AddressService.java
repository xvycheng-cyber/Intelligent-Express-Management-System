package com.express.addressbook.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.express.addressbook.common.BusinessException;
import com.express.addressbook.common.LikeEscaper;
import com.express.addressbook.common.ResultCode;
import com.express.addressbook.common.Violation;
import com.express.addressbook.dto.AddressSaveDTO;
import com.express.addressbook.entity.ExpressAddress;
import com.express.addressbook.mapper.ExpressAddressMapper;
import com.express.addressbook.parser.PhoneExtractor;
import com.express.addressbook.vo.AddressVO;
import com.express.addressbook.vo.BatchSaveResult;
import com.express.addressbook.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AddressService {

    private final ExpressAddressMapper mapper;

    public AddressService(ExpressAddressMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<AddressVO> page(int page, int size, String keyword) {
        if (page < 1) {
            throw badRequest("page", "页码必须大于等于 1");
        }
        if (size < 1 || size > 100) {
            throw badRequest("size", "每页数量必须在 1～100 之间");
        }
        String normalizedKeyword = keyword == null ? null : keyword.strip();
        if (normalizedKeyword != null && !normalizedKeyword.isEmpty()
                && normalizedKeyword.codePointCount(0, normalizedKeyword.length()) > 50) {
            throw badRequest("keyword", "关键词长度不能超过 50 个字符");
        }

        Page<ExpressAddress> mpPage = new Page<>(page, size);
        IPage<ExpressAddress> result = mapper.selectPageByKeyword(mpPage, LikeEscaper.escape(normalizedKeyword));
        List<AddressVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    public AddressVO getById(Long id) {
        requirePositiveId(id);
        ExpressAddress entity = mapper.selectById(id);
        if (entity == null) {
            throw notFound();
        }
        return toVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(AddressSaveDTO dto) {
        ExpressAddress entity = toEntity(dto);
        if (mapper.insert(entity) != 1) {
            throw databaseError();
        }
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long update(Long id, AddressSaveDTO dto) {
        requirePositiveId(id);
        ExpressAddress existing = mapper.selectById(id);
        if (existing == null) {
            throw notFound();
        }
        LambdaUpdateWrapper<ExpressAddress> update = Wrappers.lambdaUpdate();
        update.eq(ExpressAddress::getId, id)
                .set(ExpressAddress::getReceiverName, trim(dto.receiverName()))
                .set(ExpressAddress::getPhone, PhoneExtractor.normalizePhone(dto.phone()))
                .set(ExpressAddress::getFullAddress, trim(dto.fullAddress()));
        if (mapper.update(null, update) != 1) {
            throw notFound();
        }
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requirePositiveId(id);
        ExpressAddress existing = mapper.selectById(id);
        if (existing == null) {
            throw notFound();
        }
        if (mapper.deleteById(id) != 1) {
            throw notFound();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw badRequest("ids", "请选择要删除的记录");
        }
        if (ids.size() > 200) {
            throw limitExceeded("ids", "单次最多删除 200 条");
        }

        Set<Long> unique = new LinkedHashSet<>();
        for (String raw : ids) {
            if (raw == null || !raw.matches("^[1-9]\\d*$")) {
                throw badRequest("ids", "id 必须是正整数");
            }
            try {
                unique.add(Long.parseLong(raw));
            } catch (NumberFormatException ex) {
                throw badRequest("ids", "id 超出范围");
            }
        }
        if (unique.isEmpty()) {
            throw badRequest("ids", "请选择要删除的记录");
        }

        List<ExpressAddress> existing = mapper.selectBatchIds(unique);
        if (existing.isEmpty()) {
            throw notFound();
        }
        int deleted = mapper.deleteBatchIds(existing.stream().map(ExpressAddress::getId).toList());
        if (deleted == 0) {
            throw notFound();
        }
        return deleted;
    }

    @Transactional(rollbackFor = Exception.class)
    public BatchSaveResult batchSave(List<AddressSaveDTO> items) {
        if (items == null || items.isEmpty()) {
            throw badRequest("items", "没有可保存的记录");
        }
        if (items.size() > 200) {
            throw limitExceeded("items", "单次最多保存 200 条");
        }
        if (items.stream().anyMatch(item -> item == null)) {
            throw badRequest("items", "记录不能为空");
        }

        List<Long> ids = new ArrayList<>(items.size());
        for (AddressSaveDTO dto : items) {
            ExpressAddress entity = toEntity(dto);
            if (mapper.insert(entity) != 1) {
                throw databaseError();
            }
            ids.add(entity.getId());
        }
        return new BatchSaveResult(ids.size(), ids);
    }

    private ExpressAddress toEntity(AddressSaveDTO dto) {
        ExpressAddress entity = new ExpressAddress();
        entity.setReceiverName(trim(dto.receiverName()));
        entity.setPhone(PhoneExtractor.normalizePhone(dto.phone()));
        entity.setFullAddress(trim(dto.fullAddress()));
        return entity;
    }

    private AddressVO toVO(ExpressAddress entity) {
        return new AddressVO(
                entity.getId(),
                entity.getReceiverName(),
                entity.getPhone(),
                entity.getFullAddress(),
                entity.getCreateTime(),
                entity.getUpdateTime());
    }

    private String trim(String value) {
        return value == null ? null : value.strip();
    }

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw badRequest("id", "id 必须是正整数");
        }
    }

    private BusinessException badRequest(String field, String message) {
        return new BusinessException(ResultCode.VALIDATION_FAILED.code(), message,
                Map.of("violations", List.of(new Violation(field, message))));
    }

    private BusinessException limitExceeded(String field, String message) {
        return new BusinessException(ResultCode.LIMIT_EXCEEDED.code(), message,
                Map.of("violations", List.of(new Violation(field, message))));
    }

    private BusinessException notFound() {
        return new BusinessException(ResultCode.NOT_FOUND.code(), "记录不存在或已被删除");
    }

    private BusinessException databaseError() {
        return new BusinessException(ResultCode.DATABASE_ERROR.code(), "数据库操作失败，请稍后重试");
    }
}
