package com.express.addressbook.controller;

import com.express.addressbook.common.R;
import com.express.addressbook.dto.AddressBatchSaveDTO;
import com.express.addressbook.dto.AddressParseRequest;
import com.express.addressbook.dto.AddressSaveDTO;
import com.express.addressbook.dto.BatchDeleteDTO;
import com.express.addressbook.parser.AddressParserService;
import com.express.addressbook.parser.ParseInputGuard;
import com.express.addressbook.parser.ParseResult;
import com.express.addressbook.service.AddressService;
import com.express.addressbook.vo.AddressVO;
import com.express.addressbook.vo.BatchDeleteResult;
import com.express.addressbook.vo.BatchSaveResult;
import com.express.addressbook.vo.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@Validated
public class AddressController {

    private final AddressParserService parserService;
    private final AddressService addressService;

    public AddressController(AddressParserService parserService, AddressService addressService) {
        this.parserService = parserService;
        this.addressService = addressService;
    }

    @PostMapping("/parse")
    public R<ParseResult> parse(@RequestBody AddressParseRequest request) {
        String rawText = request == null ? null : request.rawText();
        ParseInputGuard.validate(rawText);
        return R.ok(parserService.parse(rawText));
    }

    @GetMapping
    public R<PageResult<AddressVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于等于 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于等于 1")
            @Max(value = 100, message = "每页数量不能超过 100") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(addressService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public R<AddressVO> get(@PathVariable @Positive(message = "id 必须是正整数") Long id) {
        return R.ok(addressService.getById(id));
    }

    @PostMapping
    public R<Map<String, Long>> create(@Valid @RequestBody AddressSaveDTO dto) {
        return R.ok(Map.of("id", addressService.create(dto)));
    }

    @PutMapping("/{id}")
    public R<Map<String, Long>> update(@PathVariable @Positive(message = "id 必须是正整数") Long id,
                                       @Valid @RequestBody AddressSaveDTO dto) {
        return R.ok(Map.of("id", addressService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable @Positive(message = "id 必须是正整数") Long id) {
        addressService.delete(id);
        return R.ok();
    }

    @PostMapping("/batch")
    public R<BatchSaveResult> batchSave(@Valid @RequestBody AddressBatchSaveDTO body) {
        return R.ok(addressService.batchSave(body.items()));
    }

    @PostMapping("/batch-delete")
    public R<BatchDeleteResult> batchDelete(@Valid @RequestBody BatchDeleteDTO body) {
        return R.ok(new BatchDeleteResult(addressService.batchDelete(body.ids())));
    }
}
