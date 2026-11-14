package com.express.addressbook.vo;

import java.util.List;

public record BatchSaveResult(int savedCount, List<Long> ids) {
}
